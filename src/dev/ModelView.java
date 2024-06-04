package dev;

import java.util.HashMap;

public class ModelView {
    String url;
    HashMap<String, Object> data = new HashMap<>();

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String,Object> getData() {
        return this.data;
    }

    public void setData(HashMap<String,Object> data) {
        this.data = data;
    }

    public void addObject(String name, Object attribute){
        data.put(name, attribute);
    }

    public void getObject(String name) {
        data.get(name);
    }
}
