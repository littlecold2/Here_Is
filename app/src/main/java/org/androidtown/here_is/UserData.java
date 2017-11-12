package org.androidtown.here_is;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by syseng on 2017-11-12.
 */

public class UserData implements Parcelable {
    // ID
    String id;
    // PW
    String pw;
    // name
    String name;
    // info
    String info;
    // youtube url
    String url;
    // profile photo index
    // String index;
    /**
     * 데이터2개를이용하여초기화하는생성자
     * @paramnum
     * @parammsg
     */
    public UserData(String ID, String PW, String NAME, String INFO, String URL) {
        id = ID;
        pw = PW;
        name = NAME;
        info = INFO;
        url = URL;
    }
    /**
     * 다른Parcel 객체를이용해초기화하는생성자
     * @paramsrc
     */
    public UserData(Parcel src) {// Parcel 객체에서읽기
        id = src.readString();
        pw = src.readString();
        name = src.readString();
        info = src.readString();
        url = src.readString();
    }
    /**
     * 내부의CREATOR 객체생성
     */
    @SuppressWarnings("unchecked")
    public static final Creator CREATOR= new Creator() {
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };
    public int describeContents() {
        return 0;
    }
    /**
     * 데이터를Parcel 객체로쓰기
     */
    public void writeToParcel(Parcel dest, int flags) {// Parcel 객체로쓰기
        dest.writeString(id);
        dest.writeString(pw);
        dest.writeString(name);
        dest.writeString(info);
        dest.writeString(url);
    }
    public String getID() {
        return id;
    }
    public void setID(String id) {
        this.id= id;
    }
    public String getPW() {
        return pw;
    }
    public void setPW(String pw) {
        this.pw= pw;
    }
    public String getNAME() {
        return name;
    }
    public void setNAME(String name) { this.name=name; }
    public String getINFO() {
        return info;
    }
    public void setINFO(String info) {
        this.info= info;
    }
    public String getURL() {
        return url;
    }
    public void setURL(String url) {
        this.url= url;
    }
}