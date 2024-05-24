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

import dev.util.Mapping;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import mg.annotation.AnnotationController;
import mg.annotation.Get;

public class FrontController extends HttpServlet{
    // List<Class<?>> ls;
    HashMap<String,Mapping> hashMap;

    public void init() throws ServletException {
        super.init();
        scan();
    }

    private void scan(){
        String pack = this.getInitParameter("controllerPackage");
        try {
            List<Class<?>> ls = getClassesInPackage(pack);
            hashMap = initializeHashMap(ls);
        } catch (Exception e) {
            e.printStackTrace();
            // ls = new ArrayList<>();
            hashMap = new HashMap<>();
        }
    }

    private List<Class<?>> getClassesInPackage(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');

        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
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
        return classes;
    }

    HashMap<String, Mapping> initializeHashMap(List<Class<?>> ls){
        HashMap<String, Mapping> map = new HashMap<>();
        for (Class<?> class1 : ls) {
            Method[] methods = class1.getDeclaredMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(Get.class)) {
                    Mapping mapping = new Mapping();
                    mapping.classe = class1.getSimpleName();
                    mapping.methode = m.getName();
                    Get annotation = m.getAnnotation(Get.class);
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
            // out.println("Liste des Controllers:");
            // if (ls.isEmpty()) {
            //     out.println("La liste est vide.");
            // }
            // for (int i = 0; i < ls.size(); i++) {
            //     out.println((i+1)+": "+ls.get(i));
            // }
            String uri = extract(request.getRequestURI());
            Mapping m = hashMap.get(uri);
            if (m == null) {
                out.println("Aucun controller n'a cette methode: "+uri);
            }else{
                out.println("Controller correspondant: "+m.classe);
            }
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