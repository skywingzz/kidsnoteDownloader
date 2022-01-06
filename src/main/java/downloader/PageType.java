package downloader;

public enum PageType {
    ALBUM("/albums/?page=",
            ".album-list-wrapper",
            ".album-meta-info",
            "앨범",
            6),

    REPORT("/reports/?page=",
            ".report-list-wrapper",
            ".report-meta-info",
            "알림장",
            6);

    private String pageUrl;
    private String listWrapper;
    private String detailMetaInfo;
    private String folderName;
    private int pageSize;


    PageType(String pageUrl, String listWrapper, String detailMetaInfo, String folderName, int pageSize) {
        this.pageUrl = pageUrl;
        this.listWrapper = listWrapper;
        this.detailMetaInfo = detailMetaInfo;
        this.folderName = folderName;
        this.pageSize = pageSize;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getListWrapper() {
        return listWrapper;
    }

    public String getDetailMetaInfo() {
        return detailMetaInfo;
    }

    public String getFolderName() {
        return folderName;
    }

    public int getPageSize() {
        return pageSize;
    }
}
