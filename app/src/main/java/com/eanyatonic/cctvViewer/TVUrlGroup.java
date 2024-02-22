package com.eanyatonic.cctvViewer;

public class TVUrlGroup {
    public String name;
    private TVUrl[] tvUrls;
    public  TVUrlGroup(String name,TVUrl[] tvUrls){
        this.name=name;
        this.tvUrls=tvUrls;
    }

    public TVUrl[] getTvUrls(){
        if(tvUrls==null){
            tvUrls=new TVUrl[url.length/2];
            for(var i=0;i<url.length;i+=2){
                tvUrls[i/2]=new TVUrl(url[i],url[i+1]);
            }
        }
        return tvUrls;
    }

    public String[] url;
}
