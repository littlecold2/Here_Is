package org.androidtown.here_is;

/**
 * Created by MIN on 2017-10-27.
 */

public class Message {
    private String id;
    private String name;
    private String intro;
    private Double latitude;
    private Double longitude;
    private int chat_room= -1;
    private String[] chat_id = {"",""};
    private String[] chat_name = {"",""};
    private String chat_text;
    private String chat_type = " ";
    private int image=0;
    private String url ;

    Message()
    {
    }

    //in// room_req
    Message(String chat_id1, String chat_id2, String name,int image,String chat_type)
    {
        this.chat_id[0]  = chat_id1;
        this.chat_id[1]  = chat_id2;
        this.chat_type =chat_type;
        this.name =name;
        this.image=image;
    }
    //in// room_set
    Message(String chat_id1, String chat_id2,String chat_name1,String chat_name2, String chat_type)
    {
        this.chat_id[0]  = chat_id1;
        this.chat_id[1]  = chat_id2;
        this.chat_name[0] = chat_name1;
        this.chat_name[1] = chat_name2;
        this.chat_type =chat_type;
    }

    //room_num, ID1,ID2,chat_type(room_set) // room_set
    Message(int chat_room, String chat_id1, String chat_id2, String chat_type)
    {
        this.chat_room = chat_room;
        this.chat_id[0]  = chat_id1;
        this.chat_id[1]  = chat_id2;
        this.chat_type =chat_type;
    }
    // ID, chat_room, chat_type, chat_text // logout //chat
    Message(String id,String name, int chat_room, String chat_type,String chat_text)
    {
        this.id = id;
        this.name = name;
        this.chat_room = chat_room;
        this.chat_type  = chat_type;
        this.chat_text  = chat_text;
    }
    // logout
    Message(int chat_room, String chat_type)
    {
        this.chat_room = chat_room;
        this.chat_type =chat_type;
    }


    //location info
    Message(String id,String name,String intro,int image,String url, Double lat,Double lng,String chat_type)
    {
        this.id  = id;
        this.name = name;
        this.intro = intro;
        this.image = image;
        this.url = url;
        latitude = lat;
        longitude= lng;
        this.chat_type =chat_type;
    }
    String getId()
    {
        return id;
    }
    String getName()
    {
        return name;
    }
    String getIntro()
    {
        return intro;
    }
    String getUrl()
    {
//            if(url==null)
//                return "no";
//            else
             return url;
    }
    Double getLat()
    {
        return latitude;
    }
    Double getLng()
    {
        return longitude;
    }
    int getChat_room(){return chat_room;}
    String[] getChat_id(){return chat_id;}
    String[] getChat_name(){return chat_name;}
    String getChat_text(){return chat_text;}
    String getChat_type(){return chat_type;}
    int getImage(){return image;}



}
