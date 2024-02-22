package com.eanyatonic.cctvViewer;

import java.util.ArrayList;

public class TVUrlGroup {
    public String name;
    private ArrayList<TVUrl> tvUrls;
    public  TVUrlGroup(String name,ArrayList<TVUrl> tvUrls){
        this.name=name;
        this.tvUrls=tvUrls;
    }

    public ArrayList<TVUrl> getTvUrls(){
        if(tvUrls==null){
            tvUrls=new ArrayList<TVUrl>();
            for(var i=0;i<url.length;i+=2){
                tvUrls.add(new TVUrl(url[i],url[i+1]));
            }
        }
        return tvUrls;
    }

    public void toUrl(){
        url=new String[tvUrls.size()*2];
        for(var i=0;i<tvUrls.size();i++){
            url[i*2]=tvUrls.get(i).name;
            url[i*2+1]=tvUrls.get(i).url;
        }
    }

    public String[] url;
}
