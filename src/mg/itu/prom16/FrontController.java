package mg.itu.prom16;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import dev.ModelView;
import dev.util.Mapping;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import mg.annotation.AnnotationController;
import mg.annotation.Get;

public class FrontController extends HttpServlet {
    List<Class<?>> ls;
    HashMap<String, Mapping> hashMap;

    public void init() throws ServletException {
        super.init();
        scan();
    }

    private void scan() throws ServletException {
        String pack = this.getInitParameter("controllerPackage");
        if (pack == null) {
            throw new ServletException("Vous devez configurer le nom du package de vos controllers dans votre web.xml");
        }
        try {
            // List<Class<?>> ls = getClassesInPackage(pack);
            ls = getClassesInPackage(pack);
            hashMap = initializeHashMap(ls);
            if (hashMap.size() == 0) {
                throw new ServletException("Vous n'avez aucun controller dans le package: " + pack);
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
                            String className = packageName + '.'
                                    + file.getName().substring(0, file.getName().length() - 6);
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
        // throw new ServletException("Le package '"+packageName+"' est vide.");
        // }
        return classes;
    }

    HashMap<String, Mapping> initializeHashMap(List<Class<?>> ls) throws ServletException {
        HashMap<String, Mapping> map = new HashMap<>();
        for (Class<?> class1 : ls) {
            Method[] methods = class1.getDeclaredMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(Get.class)) {
                    Mapping mapping = new Mapping();
                    mapping.classe = class1.getSimpleName();
                    mapping.methode = m.getName();
                    Get annotation = m.getAnnotation(Get.class);
                    if (map.containsKey(annotation.url())) {
                        throw new ServletException("Doublon: l'url '" + annotation.url() + "'' est attribuée dans "
                                + map.get(annotation.url()).classe + " et " + mapping.classe);
                    }
                    map.put(annotation.url(), mapping);
                }
            }
        }

        return map;
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        try (PrintWriter out = response.getWriter()) {
            // out.println("URL: "+request.getRequestURL());
            try {
                String uri = extract(request.getRequestURI());
                Mapping m = hashMap.get(uri);
                if (m == null) {
                    throw new ServletException("Aucun controller n'a une methode ayant le mapping : '" + uri + "'");
                } else {
                    try {
                        Object obj = Class.forName(this.getInitParameter("controllerPackage") + "." + m.classe)
                                .newInstance();
                        // out.println(obj.getClass().getDeclaredMethod(m.methode).invoke(obj));
                        Object value = obj.getClass().getDeclaredMethod(m.methode).invoke(obj);
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
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}