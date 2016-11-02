## Compiling the HANA plugin source code:
Clone the repository and add all the jars from the folder **lib** in the **Required Files Directory** to your Build Path.

## Installing GeoServer with HANA plugin on Linux:
1. Copy the folder **geoserver-2.9-SNAPSHOT** from **Required Files Directory** into your desired directory.
2. Add an environment variable to save the location of GeoServer by typing the following command:<br />
echo **"export GEOSERVER_HOME=path/geoserver-2.9-SNAPSHOT"** >> ~/.profile <br />. ~/.profile
. ~/.profile
3. Make yourself the owner of the geoserver folder: <br />sudo chown -R <user_name> **path/geoserver-2.9-SNAPSHOT/**
4. Copy **ngdbc.jar** and **gt-jdbc-hana-15-SNAPSHOT.jar** from **Required Files Directory** to **path/geoserver-2.9-SNAPSHOT/webapps/geoserver/WEB-INF/lib/**
5. Start GeoServer by changing into the directory **path/geoserver-2.9-SNAPSHOT/bin** and executing the startup script.

## documents:

1. Blog- GeoServer with SAP HANA.docx 
2. Guide- GeoServer with SAP HANA.docx 
3. Poster Presentation- GeoServer with Hana - Sayon Kumar Saha.pdf
4. Test Case Records.docx

**Required Files Directory:** https://www.dropbox.com/sh/ym0l1wjh8qkwokn/AAD3tutmaWNg1AOBcQF5Utuda?dl=0