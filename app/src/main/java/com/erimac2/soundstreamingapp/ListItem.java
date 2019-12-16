package com.erimac2.soundstreamingapp;

public class ListItem {
    private String title;
    private String imageLink;
    private Long id;

    public ListItem(String title, String imageLink, Long id)
    {
        this.title = title;
        this.imageLink = imageLink;
        this.id = id;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getImageLink()
    {
        return imageLink;
    }
    public void setImageLink(String imageLink)
    {
        this.imageLink = imageLink;
    }
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
}
