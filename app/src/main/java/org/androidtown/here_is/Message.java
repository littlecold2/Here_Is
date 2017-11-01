package org.androidtown.here_is;

/**
 * Created by MIN on 2017-10-27.
 */

public class Message {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private boolean key_chat=false;
    private String[] chat_id = {"",""};
    private String chat_text;


    Message(String id,String name, Double lat,Double lng, boolean key_chat, String id1, String id2, String chat_text)
    {
        this.id  = id;
        this.name = name;
        latitude = lat;
        longitude= lng;
        this.key_chat = key_chat;
        this.chat_id[0]  = id1;
        this.chat_id[1]  = id2;
        this.chat_text =chat_text;
    }


    Message(String id,String name, Double lat,Double lng)
    {
        this.id  = id;
        this.name = name;
        latitude = lat;
        longitude= lng;
    }
    String getId()
    {
        return id;
    }
    String getName()
    {
        return name;
    }
    Double getLat()
    {
        return latitude;
    }
    Double getLng()
    {
        return longitude;
    }
    boolean getkey_Chat(){return key_chat;}
    String[] getChat_id(){return chat_id;}
    String  getChat_text(){return chat_text;}
    void setkey_Chat(boolean key_chat){this.key_chat = key_chat;;}
    void setChat_id(String myid, String targetid)
    {
        chat_id[0] = myid;
        chat_id[1] = targetid;
    }


}
