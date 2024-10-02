package dev;

import jakarta.servlet.http.HttpSession;

public class CustomSession {
    private HttpSession session;

    public CustomSession() {
    }

    public CustomSession(HttpSession session) {
        this.session = session;
    }

    public void set(String key, Object value){
        session.setAttribute(key, value);
    }

    public Object get(String key){
        return session.getAttribute(key);
    }

    public void remove(String key){
        session.removeAttribute(key);
    }
}
