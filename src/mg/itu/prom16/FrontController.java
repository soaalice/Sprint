package mg.itu.prom16;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import dev.CustomSession;
import dev.ModelView;
import dev.util.Mapping;
import dev.util.Verb;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import mg.annotation.AnnotationController;
import mg.annotation.Get;
import mg.annotation.Post;
import mg.annotation.Url;

public class FrontController extends HttpServlet{
    List<Class<?>> ls;
    HashMap<String,Mapping> hashMap;
    CustomSession session;
    Gson gson;

    public void init() throws ServletException {
        super.init();
        gson = new Gson();
        scan();
    }

    private void scan() throws ServletException{
        String pack = this.getInitParameter("controllerPackage");
        if (pack == null) {
            throw new ServletException("Vous devez configurer le nom du package de vos controllers dans votre web.xml");
        }
        try {
            // List<Class<?>> ls = getClassesInPackage(pack);
            ls = getClassesInPackage(pack);
            hashMap = initializeHashMap(ls);
            if (hashMap.size() == 0) {
                throw new ServletException("Vous n'avez aucun controller dans le package: "+pack);
            }
        } catch (Exception e) {
            if (e instanceof ServletException) {
                throw ((ServletException) e);
            }
            e.printStackTrace();
            ls = new ArrayList<>();
            hashMap = new HashMap<>();
        }
    }

    private List<Class<?>> getClassesInPackage(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');

        Enumeration<URL> resources = classLoader.getResources(path);
        if (!resources.hasMoreElements()) {
            throw new ServletException("Le package que vous avez attribué au Servlet n'existe pas.");
        }
        // boolean isEmpty = true;
        while (resources.hasMoreElements()) {
            // isEmpty = false;
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                File directory = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                            Class<?> clazz = Class.forName(className);
                            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                                classes.add(clazz);
                            }
                        }
                    }
                }
            }
        }
        // if (isEmpty) {
        //     throw new ServletException("Le package '"+packageName+"' est vide.");
        // }
        return classes;
    }

    HashMap<String, Mapping> initializeHashMap(List<Class<?>> ls) throws ServletException{
        HashMap<String, Mapping> map = new HashMap<>();
        for (Class<?> controller : ls) {
            Method[] methods = controller.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Url.class)) {
                    Url annotation = method.getAnnotation(Url.class);

                    // Alaina raha efa tao, new-ena raha mbola
                    Mapping mapping ;
                    if (map.containsKey(annotation.value())) {
                        mapping = map.get(annotation.value());
                    } else {
                        mapping = new Mapping();
                        mapping.classe = controller.getSimpleName();
                        map.put(annotation.value(), mapping);
                    }

                    if (mapping.classe.compareToIgnoreCase(controller.getSimpleName()) != 0) {
                        throw new ServletException("L'url '"+annotation.value()+"' ne peut pas etre assignee a deux controllers differents.");
                    }

                    Verb verb = getVerb(method);
                    try {
                        mapping.setVerbMethod(verb, method);
                    } catch (Exception e) {
                        throw new ServletException(e.getMessage());
                    }
                }
            }
        }

        return map;
    }

    Verb getVerb(Method method){
        return method.isAnnotationPresent(Post.class) ? Verb.POST : Verb.GET;
    }

    String extract(String uri) {
        String[] segments = uri.split("/");
        // Si l'URI comporte au moins deux segments, retourne le reste après le premier
        // segment
        if (segments.length > 1) {
            return String.join("/", java.util.Arrays.copyOfRange(segments, 2, segments.length));
        }
        return "";
    }

    void afficher(Object value, HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception{
        if (value instanceof ModelView mw) {
            HashMap<String, Object> datas = mw.getData();
            for (String key : datas.keySet()) {
                request.setAttribute(key, datas.get(key));
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher(mw.getUrl());
            dispatcher.forward(request, response);
        } else if (value instanceof String) {
            out.println(value);
        } else {
            throw new ServletException("Le type de retour doit etre un String ou un ModelView");
        }
    }

    void afficherJson(Object value, HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception{
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = "";
        if (value instanceof ModelView mw) {
            json = gson.toJson(mw.getData());
        } else {
            json = gson.toJson(value);
        }

        out.println(json);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response, Verb verb)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        try (PrintWriter out = response.getWriter()) {
            // out.println("URL: "+request.getRequestURL());
            try {
                String uri = extract(request.getRequestURI());
                Mapping mapping = hashMap.get(uri);
                if (mapping == null) {
                    throw new ServletException("Aucun controller n'a une methode ayant le mapping : '" + uri+"'");
                }else{
                    try {
                        Object obj = Class.forName(this.getInitParameter("controllerPackage")+"."+mapping.classe).newInstance();

                        // Atao anaty HashMap ny cles sy ny parametres any
                        Enumeration<String> keys=request.getParameterNames();
                        HashMap<String,String> requestParameter=new HashMap<String,String>();
                        while(keys.hasMoreElements()){
                            String key=keys.nextElement();
                            System.out.println(key);
                            requestParameter.put(key, request.getParameter(key));
                        }

                        // Appeler la methode avec les parametres, l'objet et la session
                        Object value = mapping.invoke(requestParameter,obj,session, verb);
                        
                        // Raha manana annotation RestApi ilay methode
                        if (mapping.isRestApi(verb)) {
                            afficherJson(value, request, response, out);
                        } else{
                            afficher(value, request, response, out);
                        }
                        
                    } catch (Exception e) {
                        // out.println(e.getMessage());
                        e.printStackTrace(out);
                    }
                }    
            } catch (Exception e) {
                // e.printStackTrace(out);
                out.println(e.getMessage());
            }
        } catch (Exception e) {
            // System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       processRequest(request, response, Verb.GET);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       processRequest(request, response, Verb.POST);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}