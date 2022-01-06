package downloader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class Constants {
    @Value("${constants.domain}")
    private String domain;

    @Value("${constants.saveTargetDirectory}")
    private String saveTargetDirectory;

    @Value("${constants.targetDate.year}")
    private int targetYear;

    @Value("${constants.targetDate.month}")
    private int targetMonth;

    @Value("${constants.targetDate.day}")
    private int targetDay;

    @Value("${constants.authInformation.csrftoken}")
    private String csrftoken;

    @Value("${constants.authInformation.sessionid}")
    private String sessionid;

    public String getDomain() {
        return domain;
    }

    public String getSaveTargetDirectory() {
        return saveTargetDirectory;
    }

    public int getTargetYear() {
        return targetYear;
    }

    public int getTargetMonth() {
        return targetMonth;
    }

    public int getTargetDay() {
        return targetDay;
    }

    public String getCsrftoken() {
        return csrftoken;
    }

    public String getSessionid() {
        return sessionid;
    }
}
