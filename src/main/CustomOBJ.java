package main;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.io.FileNotFoundException;

import org.joml.Vector3f;
import org.joml.Vector2f;

public class CustomOBJ extends Shape{
    boolean hasNormals;
    boolean hasTextureCoords;
    List<ObjObject.Face>faces;
    List<Vector3f>vertices;
    List<Vector3f>normals;
    List<Vector2f>textureCoords;
    public CustomOBJ(File file){
        ObjObject model=new ObjObject();
        try{model=new OBJLoader().loadModel(file);
        }catch(FileNotFoundException e){System.out.println("file could not be located");}
        
        faces=model.getFaces();
        hasNormals=faces.get(0).hasNormals();
        hasTextureCoords=faces.get(0).hasTextureCoords();

        vertices=model.getVertices();
        normals=model.getNormals();
        textureCoords=model.getTextureCoordinates();
        createIndexedVertexList();
        //createAbsoluteVertexList();
        if(!hasNormals){computeNormals();}
        
        Random rand=new Random();
        xPos=0;
        yPos=0;
        xPos=0;
        colorList=new float[vertexList.length];
        
        for(int i=0;i<colorList.length;i+=3){
            //Vector3f color=new Vector3f(rand.nextFloat(1),rand.nextFloat(1),rand.nextFloat(1));
            Vector3f color=new Vector3f(0.8f,0.8f,0.8f);
            colorList[i]=color.x;
            colorList[i+1]=color.y;
            colorList[i+2]=color.z;
        }
    }

    public void createIndexedVertexList(){
        vertexList=new float[vertices.size()*9];
        indexList=new int[faces.size()*3];
        normalList=new float[vertexList.length];

        for(int i=0;i<faces.size();i++){
            ObjObject.Face temp=faces.get(i);
            int[]indexes=temp.getVertices();
            //for(int x=0;x<indexes.length;x++){indexes[x]-=1;}
            indexList[i*3]=indexes[0]-1;
            indexList[i*3+1]=indexes[1]-1;
            indexList[i*3+2]=indexes[2]-1;
            if(hasNormals){
                for(int f=0;f<3;f++){
                    normalList[indexes[f]*3]=normals.get(temp.getNormals()[f]-1).x;
                    normalList[indexes[f]*3+1]=-normals.get(temp.getNormals()[f]-1).y;
                    normalList[indexes[f]*3+2]=normals.get(temp.getNormals()[f]-1).z;
                }
            }
        }

        for(int i=0;i<vertices.size();i++){
            vertexList[i*3]=vertices.get(i).x;
            vertexList[i*3+1]=vertices.get(i).y;
            vertexList[i*3+2]=vertices.get(i).z;
        }
    }

    public void createAbsoluteVertexList(){
        vertexList=new float[faces.size()*9];
        normalList=new float[vertexList.length];


        for(int i=0;i<faces.size();i++){
            ObjObject.Face temp=faces.get(i);
            
            int[]vertexIndices=temp.getVertices();
            for(int f=0;f<3;f++){
                vertexList[i*9+f*3]=vertices.get(vertexIndices[f]-1).x;
                vertexList[i*9+f*3+1]=vertices.get(vertexIndices[f]-1).y;
                vertexList[i*9+f*3+2]=vertices.get(vertexIndices[f]-1).z;
            }

            if(hasNormals){
                int[]normalIndices=temp.getNormals();
                for(int f=0;f<3;f++){
                    normalList[i*9+f*3]=normals.get(normalIndices[f]-1).x;
                    normalList[i*9+f*3+1]=-normals.get(normalIndices[f]-1).y;
                    normalList[i*9+f*3+2]=normals.get(normalIndices[f]-1).z;
                }
            }
        }
    }
}
