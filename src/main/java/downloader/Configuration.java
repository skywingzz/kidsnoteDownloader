package downloader;

import org.springframework.beans.factory.annotation.Value;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Value("${domain}")
    public static String DOMAIN;

    @Value("${saveTargetDirectory}")
    public static String SAVE_TARGET_DIRECTORY;

    @Value("${targetDate.year}")
    public static int TARGET_YEAR;

    @Value("${targetDate.month}")
    public static int TARGET_MONTH;

    @Value("${targetDate.day}")
    public static int TARGET_DAY;

    @Value("${authInformation.csrftoken}")
    public static String csrftoken;

    @Value("${authInformation.sessionid}")
    public static String sessionid;

}
