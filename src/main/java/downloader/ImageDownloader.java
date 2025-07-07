package downloader;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ImageDownloader implements CommandLineRunner {
    private static Connection CONN;

    private final Constants constants;

    public ImageDownloader(Constants constants) {
        this.constants = constants;
    }

    private void imageDownload() {
        try {
            trustAllCertificates();
            System.out.println("==== 앨범 다운로드 ====");
            getPageDetail(getPageList(PageType.ALBUM), PageType.ALBUM);

            System.out.println("==== 알람장 다운로드 ====");
            getPageDetail(getPageList(PageType.REPORT), PageType.REPORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection(String url) {
        return CONN = Jsoup.connect(url)
                .cookie("csrftoken", constants.getCsrftoken())
                .cookie("sessionid", constants.getSessionid());
    }

    private List<String> getPageList(PageType pageType) throws Exception {
        String url = constants.getDomain() + pageType.getPageUrl();
        return addListByElement(pageType.getPageSize(), url, pageType.getListWrapper() + " > a");
    }

    private List<String> addListByElement(int pageSize, String url, String cssQuery) throws IOException {
        List<String> pageUrlList = new ArrayList<>();

        for (int page = 1; page <= pageSize; page++) {
            Connection connection = getConnection(url + page);
            Document html = connection.get();
            Elements links = html.select(cssQuery);
            for (Element element : links) {
                pageUrlList.add(element.attr("href"));
            }
        }

        return pageUrlList;
    }

    private void getPageDetail(List<String> pageList, PageType pageType) throws Exception {
        for(String url : pageList) {
            Connection connection = getConnection(constants.getDomain() + url);
            Document html = connection.get();
            String title = getTitle(html, pageType);

            String dateString = html.select(pageType.getDetailMetaInfo()).select(".date").html();

            Calendar date = Calendar.getInstance();
            if (PageType.ALBUM == pageType) {
                date = getDateFromText(dateString, "d. MMMM yyyy HH:mm");
            }

            if (PageType.REPORT == pageType) {
                date = getDateFromText(dateString, "EEE, MMMM d, yyyy");
            }

            int year = date.get(Calendar.YEAR);
            int month = date.get(Calendar.MONTH) + 1;

            System.out.println(year + "/" + month + " Download....");
            if(constants.getTargetYear() > 0 && year > constants.getTargetYear()) {
                continue;
            }

            if(constants.getTargetMonth() > 0 && month > constants.getTargetMonth()) {
                continue;
            }

            String convertedDate = "";
            if (PageType.ALBUM == pageType) {
                convertedDate = parseDateFormat(dateString, "d. MMMM yyyy HH:mm");
            }

            if (PageType.REPORT == pageType) {
                convertedDate = parseDateFormat(dateString, "EEE, MMMM d, yyyy");
            }

            createFolder(pageType.getFolderName() + "/" + year);

            Elements links = html.select("#img-grid-container").select("a");
            for(int index = 0 ; index < links.size() ; index++) {
                Element element = links.get(index);
                String imageLink = element.attr("data-download");
                String[] paths = imageLink.split("/");
                String[] fileInfo = splitFileInfo(paths[paths.length - 1]);
                String fileExtention = fileInfo[1];

                List<String> fileNameValues = new ArrayList<>();
                if(pageType.equals(PageType.ALBUM)) {
                    fileNameValues.add(convertedDate);
                }
                fileNameValues.addAll(Arrays.asList(title, String.valueOf(index+1)));

                String targetFileName = StringUtil.join(fileNameValues, "_") + "." + fileExtention;

                fileDownload(imageLink, constants.getSaveTargetDirectory() + "/" + pageType.getFolderName() + "/" + year, targetFileName);
            }
        }
    }

    private String getTitle(Document html, PageType pageType) throws Exception {
        String titleClass = ".sub-header-title";
        html.select(titleClass + " > div.tooltip").remove();

        String title = html.select(titleClass).text();
        if(pageType.equals(PageType.REPORT)) {
            return parseDateFormat(title, "EEE, MMMM d, yyyy");
        }
        title = title.replace(":", "");
        title = title.replace("-", "");
        title = title.replace(")", "");
        return title;
    }

    private void createFolder(String folderName) throws Exception {
        Path path = Paths.get(constants.getSaveTargetDirectory() + "/" + folderName);
        Files.createDirectories(path);
    }

    private static void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustManagers = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManagers, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }

    private void fileDownload(String fileUrl, String outputPath, String imageName) throws Exception {
        try(InputStream in = new URL(fileUrl).openStream()) {
            Path imagePath = Paths.get(outputPath + "/" + imageName);
//            Thread.sleep(1000L);
            Files.copy(in, imagePath);
        } catch (FileAlreadyExistsException e) {
            String[] fileInfo = splitFileInfo(imageName);
            String newImageName = fileInfo[0] + "_1." + fileInfo[1];
            System.out.println("File Exists Rename File : " + imageName + "->" + newImageName );
            fileDownload(fileUrl, outputPath, newImageName);

        }
    }

    private String[] splitFileInfo(String fullName) {
        fullName = fullName.replaceFirst("[?][^?]+$", "");

        return fullName.split("\\.");
    }

    private String parseDateFormat(String dateString) throws Exception {
        return parseDateFormat(dateString, "EEE, MMMM d, yyyy");
    }

    private String parseDateFormat(String dateString, String format) throws Exception {
        SimpleDateFormat targetDt = new SimpleDateFormat("yyyy-MM-dd");
        return targetDt.format(getDateFromText(dateString, format).getTime());
    }

    private Calendar getDateFromText(String dateString) throws Exception {
        return getDateFromText(dateString, "EEE, MMMM d, yyyy");
    }

    private Calendar getDateFromText(String dateString, String format) throws Exception {
        SimpleDateFormat sourceDt = new SimpleDateFormat(format, Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sourceDt.parse(dateString));

        return calendar;
    }

    @Override
    public void run(String... args) {
        imageDownload();

    }
}
