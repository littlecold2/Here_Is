package org.androidtown.here_is;

/**
 * Created by MIN on 2017-10-27.
 */

public class Message {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;
    private int chat_room=-1;
    private String[] chat_id = {"",""};
    private String chat_text;
    private String chat_type = " ";

    Message()
    {
    }

    //in// room_set
    Message(String chat_id1, String chat_id2, String chat_type)
    {
        this.chat_id[0]  = chat_id1;
        this.chat_id[1]  = chat_id2;
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
    Message(String id, int chat_room, String chat_type,String chat_text)
    {
        this.id = id;
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
    int getChat_room(){return chat_room;}
    String[] getChat_id(){return chat_id;}
    String getChat_text(){return chat_text;}
    String getChat_type(){return chat_type;}
    //  void setkey_Chat(boolean key_chat){this.key_chat = key_chat;;}
    //   void setChat_id(String myid, String targetid)
    //  {
    //     chat_id[0] = myid;
    //    chat_id[1] = targetid;
    //}


}
