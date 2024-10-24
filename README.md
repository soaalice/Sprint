# Sprint10

Pour utiliser le FrontController il faut configurer votre web.xml comme tel:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.1" xmlns="http://xmlns.jcp.org/xml/ns/javaee" 
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">
    <servlet>
        <servlet-name>FrontController</servlet-name>
        <servlet-class>mg.itu.prom16.FrontController</servlet-class>
        <init-param>
            <param-name>controllerPackage</param-name>
            <param-value>chemin.de.votre.package</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>FrontController</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>
```

Vos controllers devront avoir l'annotation @AnnotationController, et leurs fonctions @Url("votreUrl").
Annotez-les @Get ou @Post si necessaires.

Si les fonctions de vos controllers recquierent des parametres, annotez-les avec @Param(name = "nomDuParametre").

Ces fonctions doivent retourner soit un String soit un ModelView (dont les datas sont definies)
Sinon elles peuvent egalement retourner des objets si la fonction est annotee @RestApi.

Assurez-vous que la page désignée par l'url de votre ModelView existe.
Ne pas annoter un meme url a deux controllers differents.