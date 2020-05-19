package com.gx.demo.model;

import org.springframework.beans.factory.annotation.Value;

/**
 * @ClassName PageUtil
 * @Description 分页信息Model
 * @Authtor guoxiang
 * @Date 2020/5/15 9:32
 **/
public class PageUtil {
    private int pageSize = 2;
    private int currentPage = 0;
    private int sumPage;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getSumPage() {
        return sumPage;
    }

    public void setSumPage(int sumPage) {
        this.sumPage = sumPage;
    }
}
