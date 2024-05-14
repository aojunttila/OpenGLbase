package main;

import java.util.Random;

import org.joml.Vector3f;

public class Shape {
    float xPos;
    float yPos;
    float zPos;
    float[]vertexList;
    float[]colorList;
    float[]normalList;
    int[]indexList;

    public Shape(){
        
    }

    public Shape(boolean init){
        Random rand=new Random();
        vertexList=new float[9];
        colorList=new float[9];
        normalList=new float[9];
        for(int i=0;i<9;i++){
            vertexList[i]=rand.nextFloat(3f);
            colorList[i]=rand.nextFloat(1);
        }
        computeNormals();
        //computeIndices();
        indexList=new int[]{0,1,2};
    }

    public Shape(float[]vList){
        Random rand=new Random();
        vertexList=vList;
        colorList=new float[vertexList.length];
        normalList=new float[vertexList.length];
        for(int i=0;i<vertexList.length;i++){
            colorList[i]=rand.nextFloat(0.1f)+0.9f;
        }
        computeNormals();
    }

    public Shape(float[]vList,float[]nList){
        Random rand=new Random();
        vertexList=vList;
        colorList=new float[vertexList.length];
        normalList=nList;
        for(int i=0;i<vertexList.length;i++){
            colorList[i]=rand.nextFloat(1);
        }
    }

    public float[] getVertexList(){
        return vertexList;
    }

    public float[] getColorList(){
        return colorList;
    }

    public float[] getNormalList(){
        return normalList;
    }

    public int[] getIndexList(){
        return indexList;
    }

    public void setColorList(float[]cList){
        colorList=cList;
    }

    public void setColor(float[]color){
        for(int i=0;i<colorList.length;i+=3){
            colorList[i]=color[0];
            colorList[i+1]=color[1];
            colorList[i+2]=color[2];
        }
    }

    public void computeNormals(){
        Vector3f vector1,vector2;
        for(int i=0;i<vertexList.length;i+=9){
            vector1=new Vector3f(vertexList[i]-vertexList[i+3],vertexList[i+1]-vertexList[i+4],vertexList[i+2]-vertexList[i+5]);
            vector2=new Vector3f(vertexList[i+3]-vertexList[i+6],vertexList[i+4]-vertexList[i+7],vertexList[i+5]-vertexList[i+8]);
            vector1.cross(vector2);
            vector1.normalize();
            for(int f=0;f<9;f+=3){
                normalList[i+f]=vector1.x;
                normalList[i+f+1]=vector1.y;
                normalList[i+f+2]=vector1.z;
            }
            //Util.printList(normalList);
        }
    }

    public void computeIndices(){
        indexList=new int[vertexList.length/3];
        for(int i=0;i<indexList.length/3;i++){
            indexList[i]=i;
        }
    }

    //Mutator methods

    public Shape transform(float x,float y,float z){
        for(int i=0;i<vertexList.length;i+=3){
            vertexList[i]+=x;
            vertexList[i+1]+=y;
            vertexList[i+2]+=z;
        }
        xPos+=x;
        yPos+=y;
        zPos+=z;
        //Util.printList(vertexList);
        return this;
    }

    public Shape setPos(float x,float y,float z){
        for(int i=0;i<vertexList.length;i+=3){
            vertexList[i]+=(x-xPos);
            vertexList[i+1]+=(y-yPos);
            vertexList[i+2]+=(z-zPos);
        }
        xPos=x;
        yPos=y;
        zPos=z;
        return this;
    }

    public Shape scale(float x,float y,float z){
        for(int i=0;i<vertexList.length;i+=3){
            vertexList[i]=(vertexList[i]-xPos)*x+xPos;
            vertexList[i+1]=(vertexList[i+1]-yPos)*y+yPos;
            vertexList[i+2]=(vertexList[i+2]-zPos)*z+zPos;
        }
        return this;
    } 

    public Shape rotatex(float angle){
        for(int i=0;i<vertexList.length;i+=3){
            float s=(float)Math.sin(Math.toRadians(angle));
            float c=(float)Math.cos(Math.toRadians(angle));
            float s2=(float)Math.sin(Math.toRadians(-angle));
            float c2=(float)Math.cos(Math.toRadians(-angle));
            vertexList[i+1]-=yPos;
            vertexList[i+2]-=zPos;
            float ynew=vertexList[i+1]*c-vertexList[i+2]*s;
            float znew=vertexList[i+1]*s+vertexList[i+2]*c;
            float nynew=normalList[i+1]*c2-normalList[i+2]*s2;
            float nznew=normalList[i+1]*s2+normalList[i+2]*c2;
            vertexList[i+1]=ynew+yPos;
            vertexList[i+2]=znew+zPos;
            normalList[i+1]=nynew;
            normalList[i+2]=nznew;
        }
        return this;
    }

    public Shape rotatey(float angle){
        for(int i=0;i<vertexList.length;i+=3){
            float s=(float)Math.sin(Math.toRadians(angle));
            float c=(float)Math.cos(Math.toRadians(angle));
            vertexList[i]-=xPos;
            vertexList[i+2]-=zPos;
            float xnew=vertexList[i]*c-vertexList[i+2]*s;
            float znew=vertexList[i]*s+vertexList[i+2]*c;
            float nxnew=normalList[i]*c-normalList[i+2]*s;
            float nznew=normalList[i]*s+normalList[i+2]*c;
            vertexList[i]=xnew+xPos;
            vertexList[i+2]=znew+zPos;
            normalList[i]=nxnew;
            normalList[i+2]=nznew;
        }
        return this;
    }

    public Shape rotatez(float angle){
        for(int i=0;i<vertexList.length;i+=3){
            float s=(float)Math.sin(Math.toRadians(angle));
            float c=(float)Math.cos(Math.toRadians(angle));
            float s2=(float)Math.sin(Math.toRadians(-angle));
            float c2=(float)Math.cos(Math.toRadians(-angle));
            vertexList[i]-=xPos;
            vertexList[i+1]-=yPos;
            float xnew=vertexList[i]*c-vertexList[i+1]*s;
            float ynew=vertexList[i]*s+vertexList[i+1]*c;
            float nxnew=normalList[i]*c2-normalList[i+1]*s2;
            float nynew=normalList[i]*s2+normalList[i+1]*c2;
            vertexList[i]=xnew+xPos;
            vertexList[i+1]=ynew+yPos;
            normalList[i]=nxnew;
            normalList[i+1]=nynew;
        }
        return this;
    }

    public Shape moveOrigin(float x,float y,float z){
        xPos+=x;
        yPos+=y;
        zPos+=z;
        return this;
    }
}
