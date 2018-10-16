package com.tuputech.mvp.entity;

/**
 * Created by RogerOu on 2018/5/23.
 */
public class Product {

    private String mTag;
    private String mPhoto;
    private String mProduct;
    private String mSummary;
    private String mAge;
    private String mUrl;
    private String mSex;

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
    }

    public String getProduct() {
        return mProduct;
    }

    public void setProduct(String product) {
        mProduct = product;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getAge() {
        return mAge;
    }

    public void setAge(String age) {
        mAge = age;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (mTag != null ? !mTag.equals(product.mTag) : product.mTag != null) return false;
        if (mPhoto != null ? !mPhoto.equals(product.mPhoto) : product.mPhoto != null) return false;
        if (mProduct != null ? !mProduct.equals(product.mProduct) : product.mProduct != null)
            return false;
        if (mSummary != null ? !mSummary.equals(product.mSummary) : product.mSummary != null)
            return false;
        if (mAge != null ? !mAge.equals(product.mAge) : product.mAge != null) return false;
        if (mUrl != null ? !mUrl.equals(product.mUrl) : product.mUrl != null) return false;
        return mSex != null ? mSex.equals(product.mSex) : product.mSex == null;
    }

    @Override
    public int hashCode() {
        int result = mTag != null ? mTag.hashCode() : 0;
        result = 31 * result + (mPhoto != null ? mPhoto.hashCode() : 0);
        result = 31 * result + (mProduct != null ? mProduct.hashCode() : 0);
        result = 31 * result + (mSummary != null ? mSummary.hashCode() : 0);
        result = 31 * result + (mAge != null ? mAge.hashCode() : 0);
        result = 31 * result + (mUrl != null ? mUrl.hashCode() : 0);
        result = 31 * result + (mSex != null ? mSex.hashCode() : 0);
        return result;
    }

    public String getSex() {
        return mSex;
    }

    public void setSex(String sex) {
        mSex = sex;
    }
}
