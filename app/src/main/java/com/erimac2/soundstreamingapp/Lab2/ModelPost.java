package com.erimac2.soundstreamingapp.Lab2;

import android.view.Display;

public class ModelPost {

    int id;
    int userId;
    String title;
    String bodyText;
    int count;

    public ModelPost()
    {
    }

    public ModelPost(int id, int userId, String title, String bodyText)
    {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.bodyText = bodyText;
        this.count = 0;
    }
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public int getUserId()
    {
        return userId;
    }
    public void setUserId(int userId)
    {
        this.userId = userId;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getBodyText()
    {
        return bodyText;
    }
    public void setBodyText(String bodyText)
    {
        this.bodyText = bodyText;
    }
    public int getCount()
    {
        return count;
    }
    public void setCount(int count)
    {
        this.count = count;
    }
}
