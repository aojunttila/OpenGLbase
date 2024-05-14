//471 lines -> 224 lines

//add skybox
//add texture support
//screen space ambient occlusion

/*Completed:
 * Create OBJ parser
 * Load OBJ into scene
 * Normal OBJ support
 * Add indexed rendering
 * Fix index normal bug
 * FPS counter
 * fix multiple obj rendering
 * rewrite shape classes for indexing
 * 
 */

 package main;
 import static org.lwjgl.glfw.GLFW.*;
 import static org.lwjgl.opengl.GL11C.*;
 import static org.lwjgl.opengl.GL15C.*;
 import static org.lwjgl.opengl.GL20C.*;
 import static org.lwjgl.opengl.GL30C.glBindVertexArray;
 import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
 import static org.lwjgl.system.MemoryStack.stackPush;
 import static org.lwjgl.system.MemoryUtil.*;
 
 import java.io.File;
 import java.nio.*;
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.joml.*;
 import org.joml.Math;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
 import org.lwjgl.glfw.GLFWKeyCallback;
 import org.lwjgl.glfw.GLFWVidMode;
 import org.lwjgl.opengl.*;
 import org.lwjgl.system.*;
 
 public class Main {
     static int width=800;
     static int height=600;
 
     //performance
     boolean doDynamicUpdates=false;
    
     boolean useIndexedDrawing=false;
     int frames;
     long lastSplitTime;
     Vector3f sun=new Vector3f(0.0f,1.0f,0f);
     Random rand=new Random();
     private boolean[] keyDown=new boolean[GLFW_KEY_LAST + 1];
     double pitch=0;boolean viewing;
     double yaw=0;int location;
     double rotationTracker;
     float sensitivity=0.1f;
     float[]vertexList,colorList,normalList;
     int[]indexList;
     ArrayList<Shape>shapeList=new ArrayList<Shape>();
     private final Matrix4f mat=new Matrix4f();
     private final Quaternionf orientation=new Quaternionf();
     private final Vector3f position=new Vector3f(0, 2, 5).negate();
     int mouseX,mouseY;
     int normalvbo,colorvbo,ebo,vbo,vao;
 
     public Main(){
         run();
     }
 
     public void createShapes(){
         //shapeList.add(new CustomOBJ(new File("src/main/models/Bunny.obj")).scale(100,100,100).transform(0, -10, 0));
         shapeList.add(new CustomOBJ(new File("src/main/models/mountain1.obj")).scale(100,100,100).transform(0, -10, 0));//.scale(100f,100f,100f)
         //shapeList.add(new CustomOBJ(new File("src/main/models/teapot.obj")).scale(1,1,1).transform(0, 0, 0));
         //shapeList.add(new CustomOBJ(new File("src/main/models/K2.obj")).scale(1,1,1));//.scale(100f,100f,100f)
         //shapeList.add(new CustomOBJ(new File("src/main/models/tromso.obj")).scale(1,1,1));
         //shapeList.add(new Shape(true).transform(0, 0, 1));
         //shapeList.add(new Shape(true));
         fractalTree(7,new Vector3f(0,40,0),new Vector3f(0,0,5));
         for(int i=0;i<10000;i++){
             
             //shapeList.add(new Shape(true));
             shapeList.add(new Cube().transform(rand.nextFloat(300), 0, rand.nextFloat(300)).scale(1f,rand.nextFloat(2f),1f));
         }
     }
 
     public void mutateShapes(){
         for(int i=0;i<shapeList.size();i++){
             //shapeList.get(i).rotatex(0.1f);
             //shapeList.get(i).rotatex(rand.nextFloat(1)-0.3f).rotatey(rand.nextFloat(1)-0.3f).rotatez(rand.nextFloat(1)-0.3f);
             //shapeList.get(i).scale(rand.nextFloat(0.1f)+0.95f,rand.nextFloat(0.1f)+0.95f,rand.nextFloat(0.1f)+0.95f);
             //shapeList.get(i).transform(rand.nextFloat(0.1f)-0.05f,rand.nextFloat(0.1f)-0.05f,rand.nextFloat(0.1f)-0.05f);
         }
         //computeNormals();
         //constructLists();
         updateVertexList();
     }
 
     public void constructLists(){
         ArrayList<Float>vertexArrayList=new ArrayList<Float>(0);
         ArrayList<Float>colorArrayList=new ArrayList<Float>(0);
         ArrayList<Integer>indexArrayList=new ArrayList<Integer>(0);
         
         for(int i=0;i<shapeList.size();i++){
             float[]tempVertex=shapeList.get(i).getVertexList();
             float[]tempColor=shapeList.get(i).getColorList();
             int[]tempIndices=shapeList.get(i).getIndexList();
             
             if(tempIndices!=null){
                 for(int f=0;f<tempIndices.length;f++){
                     indexArrayList.add((Integer)(tempIndices[f]+vertexArrayList.size()/3));
                 }
             }
             for(int f=0;f<tempVertex.length;f++){
                 vertexArrayList.add(tempVertex[f]);
                 colorArrayList.add(tempColor[f]);
             }
         }         
         vertexList=new float[vertexArrayList.size()];
         colorList=new float[vertexArrayList.size()];
         indexList=new int[indexArrayList.size()];
         for(int g=0;g<vertexArrayList.size();g++){
             vertexList[g]=vertexArrayList.get(g);
             colorList[g]=colorArrayList.get(g);
         }
         if(indexList.length!=0){
             for(int g=0;g<indexArrayList.size();g++){
                 indexList[g]=indexArrayList.get(g).intValue();
             }
         }
         ///Util.printList(vertexList);
         //Util.printList(indexList);
         //System.out.println(indexList.length);
     }
 
     public void updateVertexList(){
         int index=0;
         for(int i=0;i<shapeList.size();i++){
             float[]tempVertex=shapeList.get(i).getVertexList();
             for(int f=0;f<tempVertex.length;f++){
                 vertexList[index]=tempVertex[f];
                 index++;
             }
         }
     }
 
     public void computeNormals(){
         normalList=new float[vertexList.length];
         int index=0;
         for(int i=0;i<shapeList.size();i++){
             float[]tempNormals=shapeList.get(i).getNormalList();
             
             for(int f=0;f<tempNormals.length;f++){
                 normalList[index+f]=tempNormals[f];
             }
             index+=tempNormals.length;
         }
     }
 
     int maxOrder=0;
     public void fractalTree(int order, Vector3f position, Vector3f transform){
        //transform =, x rotation, y rotation, length
        if(order>maxOrder){maxOrder=order;}
        int invorder=(maxOrder-order);
        if(order>-1){
            if(transform.y%360==270&&position.x<0){transform.x*=0.3;}
            if(transform.y%360==90&&position.x>0){transform.x*=0.3;}
            if(transform.y%360==180&&position.z>0){transform.x*=0.3;}
            if(transform.y%360==0&&position.z<0){transform.x*=0.3;}
            if(transform.y%360==180&&position.x<0){transform.x*=0.5;}
            if(transform.y%360==0&&position.x>0){transform.x*=0.5;}
            if(transform.y%360==90&&position.z>0){transform.x*=0.5;}
            if(transform.y%360==270&&position.z<0){transform.x*=0.5;}
            if(transform.y%360==0&&position.x<0){transform.x*=0.6;}
            if(transform.y%360==180&&position.x>0){transform.x*=0.6;}
            if(transform.y%360==270&&position.z>0){transform.x*=0.6;}
            if(transform.y%360==90&&position.z<0){transform.x*=0.6;}

            Shape tempCube=new Cube()
                .moveOrigin(0.0f,-0.5f,0.0f)
                .scale(0.2f,transform.z,0.2f)
                .rotatex(transform.x)
                .rotatey(transform.y)
                .setPos(position.x,position.y,position.z);
            shapeList.add(tempCube);

            float[]tempVertex=tempCube.getVertexList();
            position.x=(tempVertex[60]+tempVertex[69])/2;
            position.y=(tempVertex[61]+tempVertex[70])/2;
            position.z=(tempVertex[62]+tempVertex[71])/2;

            transform.z=transform.z*0.8f;
            if(transform.x==0){transform.x=10;}
            transform.x+=20;
            int variance=40;
            int subtract=20;
            
            fractalTree(order-1,new Vector3f(position.x,position.y,position.z),new Vector3f(transform.x,transform.y,transform.z));
            fractalTree(order-1,new Vector3f(position.x,position.y,position.z),new Vector3f(transform.x,transform.y+90,transform.z));
            fractalTree(order-1,new Vector3f(position.x,position.y,position.z),new Vector3f(transform.x,transform.y+180,transform.z));
            fractalTree(order-1,new Vector3f(position.x,position.y,position.z),new Vector3f(transform.x,transform.y+270,transform.z));
        }
     }
 
     public void run(){
         if (!glfwInit())
             throw new AssertionError("Unable to initialize GLFW");
         glfwDefaultWindowHints();
         glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
         glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
         glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
         glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
         glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
         glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
         glfwWindowHint(GLFW_CURSOR, GLFW_CURSOR_DISABLED);
 
         long window=glfwCreateWindow(width,height,"OpenGL",NULL,NULL);
         if (window==NULL){throw new AssertionError("Failed to create the GLFW window");}
         GLFWKeyCallback keyCallback;GLFWFramebufferSizeCallback fbCallback;Callback debugProc;
 
         glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
             public void invoke(long window,int width,int height) {
                 if (width>0&&height>0&&(Main.width!=width||Main.height!=height)) {
                     Main.width=width;Main.height=height;
         }}});
 
         glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
             public void invoke(long window, int key, int scancode, int action, int mods) {
                 if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
                     glfwSetWindowShouldClose(window, true);
                 }else if (key >= 0 && key <= GLFW_KEY_LAST) {
                     keyDown[key] = action == GLFW_PRESS || action == GLFW_REPEAT;
         }}});
 
         GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
         glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
 
         try (MemoryStack frame=stackPush()){
             IntBuffer framebufferSize = frame.mallocInt(2);
             nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
             width=framebufferSize.get(0);height=framebufferSize.get(1);
         }
 
         glfwSetCursorPosCallback(window, (long window2, double xpos, double ypos) -> {
             float deltaX=(float)(xpos-mouseX);
             float deltaY=(float)(ypos-mouseY);
             mouseX=(int)xpos;mouseY=(int)ypos;
             double prevPitch=pitch;
             float rotateY=deltaY*(0.01f*sensitivity);
             float rotateX=deltaX*(0.01f*sensitivity);
             rotationTracker+=rotateX/2;
             yaw+=rotateX;pitch+=rotateY;
             if(pitch>1.5){pitch=1.5;rotateY=(float)(pitch-prevPitch);}
             if(pitch<-1.5){pitch=-1.5;rotateY=(float)(pitch-prevPitch);}
             orientation.rotateY(rotateX);orientation.rotateLocalX(rotateY);
         });
 
         glfwSetMouseButtonCallback(window,(long window4,int button,int action,int mods) -> {
             if(button==GLFW_MOUSE_BUTTON_1&&action==GLFW_PRESS){viewing=true;}else{viewing=false;}});
         glfwSetCursorEnterCallback(window, (windowHandle, entered) -> {
             if(entered){glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
             }else{glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);}});
 
         createShapes();
         constructLists();
         computeNormals();
 
         glfwMakeContextCurrent(window);
         glfwSwapInterval(1);
         glfwShowWindow(window);
         GL.createCapabilities();
         debugProc=GLUtil.setupDebugMessageCallback();
         vao = glGenVertexArrays();
         FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(colorList.length);
         colorBuffer.put(colorList).flip();
         glBindVertexArray(vao);
         vbo=glGenBuffers();
         ebo=glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, vbo);
         glBufferData(GL_ARRAY_BUFFER, vertexList.length*16, GL_DYNAMIC_DRAW);
         glBufferSubData(GL_ARRAY_BUFFER, 0, vertexList);
         glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(0);
         colorvbo = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, colorvbo);
         glBufferData(GL_ARRAY_BUFFER, colorList.length*16, GL_DYNAMIC_DRAW);
         glBufferSubData(GL_ARRAY_BUFFER, 0, colorList);
         glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(1);
         normalvbo = glGenBuffers();
         glBindBuffer(GL_ARRAY_BUFFER, normalvbo);
         glBufferData(GL_ARRAY_BUFFER, normalList.length*16, GL_DYNAMIC_DRAW);
         glBufferSubData(GL_ARRAY_BUFFER, 0, normalList);
         glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(2);
         
         if(indexList.length==0){
             int length=vertexList.length;indexList=new int[length];
             for(int i=0;i<length;i++){indexList[i]=i;}
         }
 
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
         glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexList, GL_STATIC_DRAW);
         glClearColor(0.08f, 0.08f, 0.08f, 1.0f);
         glEnable(GL_DEPTH_TEST);
         glEnable(GL_BLEND);
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
         int program=glCreateProgram();
         int vshader=createShader("vertex");
         int fshader=createShader("fragment");
         glAttachShader(program,vshader);
         glAttachShader(program,fshader);
         glLinkProgram(program);
         int linked = glGetProgrami(program, GL_LINK_STATUS);
         String programLog = glGetProgramInfoLog(program);
         if(programLog.trim().length()>0){System.err.println(programLog);}
         if(linked==0){throw new AssertionError("Could not link program");}
         glUseProgram(program);int viewProjUniform = glGetUniformLocation(program, "viewProj");
         int lightUniform = glGetUniformLocation(program, "light");
         Matrix4f viewProj=new Matrix4f();float angle=0.0f;long lastTime=System.nanoTime();
 
         lastSplitTime=System.nanoTime();
 
         while (!glfwWindowShouldClose(window)) {
             glfwPollEvents();long thisTime=System.nanoTime();float delta=(thisTime-lastTime)/1E9f;
             angle+=delta;lastTime=thisTime;glViewport(0,0,width,height);glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
             viewProj.setPerspective((float)Math.toRadians(43.0f),(float)width/height,0.1f,100.0f)
               .lookAt(0,0.5f,3,0,0,0,0, 1, 0);
             try(MemoryStack stack=stackPush()) {
                 glUniformMatrix4fv(viewProjUniform, false, updateMatrices(delta).get(stack.mallocFloat(16)));
                 glUniform3fv(lightUniform,new float[]{sun.x,sun.y,sun.z});}

             glDisable(GL_CULL_FACE);
             if(useIndexedDrawing){glDrawElements(GL_TRIANGLES, indexList.length, GL_UNSIGNED_INT, ebo);
             }else{glDrawElements(GL_TRIANGLES, indexList.length, GL_UNSIGNED_INT, 0);}
             glfwSwapBuffers(window);
             fpsUpdate();
         }
         if(debugProc!=null){
             debugProc.free();}
         keyCallback.free();
         fbCallback.free();
         glfwDestroyWindow(window);
     }
 
     int fps;
     public void fpsUpdate(){
         frames++;
         if(lastSplitTime+1000000000<System.nanoTime()){
             lastSplitTime+=1000000000;
             fps=frames;
             System.out.println(fps);
             frames=0;
         }
     }
 
     public int createShader(String type){
         int shader=0;
         if(type.equals("vertex")){
             shader = glCreateShader(GL_VERTEX_SHADER);
             glShaderSource(shader, """
                 #version 330 core
                 uniform mat4 viewProj;
                 layout(location = 0) in vec3 position;
                 layout(location = 1) in vec3 colors;
                 layout(location = 2) in vec3 normal;
                 uniform vec3 light;
                 out vec3 vertexColor;
                 vec3 lightAngle = vec3(0.0, 0.0, 0.0);
                 vec3 tempColor;
                 float angle;
                 void main(void) {
                     angle = acos(dot(normal, light))+1;
                     gl_Position = viewProj * vec4(position, 1.0);
                     tempColor = (colors / 5)*angle;
                     vertexColor = vec3(tempColor.x, tempColor.y, tempColor.z);
                 }""");}else{
             shader = glCreateShader(GL_FRAGMENT_SHADER);
             glShaderSource(shader, """
                 #version 330 core
                 uniform int clouds;
                 in vec3 vertexColor;
                 out vec4 color;
                 void main(void) {
                     color = vec4(vertexColor.x, vertexColor.y, vertexColor.z, 1);
                 }""");}
         glCompileShader(shader);
         int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
         String shaderLog = glGetShaderInfoLog(shader);
         if (shaderLog.trim().length()>0){System.err.println(shaderLog);}
         if (compiled==0){throw new AssertionError("Could not compile shader");}
         return shader;
     }
 
     float fov=90f;
     private Matrix4f updateMatrices(float dt) {
         float speed=0.1f;
         if(keyDown[GLFW_KEY_LEFT_CONTROL]){speed = 0.3f;}
         //if(keyDown[GLFW_KEY_Q]){rotateZ-=3f;}
         //if(keyDown[GLFW_KEY_E]){rotateZ+=3f;}
         if(keyDown[GLFW_KEY_R]){fov=(fov-30)*0.5f+30f;}else{fov=(fov-90)*0.5f+90f;;}
         if(keyDown[GLFW_KEY_W]){position.add(new Vector3f(speed*(float)Math.sin(-yaw),0f,speed*(float)Math.cos(-yaw)));}
         if(keyDown[GLFW_KEY_S]){position.add(new Vector3f(-speed*(float)Math.sin(-yaw),0f,-speed*(float)Math.cos(-yaw)));}
         if(keyDown[GLFW_KEY_A]){position.add(new Vector3f(-speed*(float)Math.sin(-yaw-Math.PI/2),0f,-speed*(float)Math.cos(-yaw-Math.PI/2)));}
         if(keyDown[GLFW_KEY_D]){position.add(new Vector3f(speed*(float)Math.sin(-yaw-Math.PI/2),0f,speed*(float)Math.cos(-yaw-Math.PI/2)));}
         if(keyDown[GLFW_KEY_SPACE]){position.add(new Vector3f(0.0f,-1f*speed,0.0f));}
         if(keyDown[GLFW_KEY_LEFT_SHIFT]){position.add(new Vector3f(0.0f,1f*speed,0.0f));}
         
         updateShapes();
         //orientation.rotateLocalZ(-(rotateZ*dt*speed));
         mat.setPerspective((float)Math.toRadians(fov),(float)width/height,0.1f,1000.0f).rotate(orientation).translate(position);
         //orientation.rotateLocalZ(rotateZ*dt*speed);
         return mat;
     }
    
     float speed2=0.0f;
     float rotateZ=0;
     double smoothRotationTracker=0;
     private Matrix4f updateMatrices2(float dt) {
        float speed=0.1f;
        
        if(keyDown[GLFW_KEY_LEFT_CONTROL]){speed = 0.3f;}
        if(keyDown[GLFW_KEY_Q]){rotateZ-=0.01f;}
        if(keyDown[GLFW_KEY_E]){rotateZ+=0.01f;}
        //if(keyDown[GLFW_KEY_R]){fov=(fov-30)*0.5f+30f;}else{fov=(fov-90)*0.5f+90f;}
        if(keyDown[GLFW_KEY_W]){speed2=(speed2-20)*0.9f+20f;fov=(fov-110)*0.95f+110f;}
        if(keyDown[GLFW_KEY_S]){speed2=(speed2-5)*0.95f+5f;fov=(fov-90)*0.95f+90f;}
        //if(keyDown[GLFW_KEY_W]){position.add(new Vector3f(speed*(float)Math.sin(-yaw),0f,speed*(float)Math.cos(-yaw)));}
         //if(keyDown[GLFW_KEY_S]){position.add(new Vector3f(-speed*(float)Math.sin(-yaw),0f,-speed*(float)Math.cos(-yaw)));}
        if(keyDown[GLFW_KEY_A]){position.add(new Vector3f(-speed*(float)Math.sin(-yaw-Math.PI/2),0f,-speed*(float)Math.cos(-yaw-Math.PI/2)));}
        if(keyDown[GLFW_KEY_D]){position.add(new Vector3f(speed*(float)Math.sin(-yaw-Math.PI/2),0f,speed*(float)Math.cos(-yaw-Math.PI/2)));}
        if(keyDown[GLFW_KEY_SPACE]){position.add(new Vector3f(0.0f,-0.1f*speed*speed2,0.0f));}
        if(keyDown[GLFW_KEY_LEFT_SHIFT]){position.add(new Vector3f(0.0f,0.1f*speed*speed2,0.0f));}
        position.add(orientation.positiveZ(new Vector3f()).mul(dt * speed2));


        smoothRotationTracker=(smoothRotationTracker-rotationTracker)*0.9+rotationTracker;
        System.out.println(smoothRotationTracker);
        if(smoothRotationTracker>0.2){smoothRotationTracker=0.2;}
        if(smoothRotationTracker<-0.2){smoothRotationTracker=-0.2;}
        rotateZ-=smoothRotationTracker*0.015*speed2;
        
        rotationTracker*=0.95f;
        rotateZ=rotateZ*0.95f;


        updateShapes();
        
        //orientation.rotateLocalZ(-(rotateZ*dt*speed));
        orientation.rotateLocalZ((float)Math.toRadians(360)-(rotateZ));
        mat.setPerspective((float)Math.toRadians(fov),(float)width/height,0.1f,1000.0f).rotate(orientation).translate(position);
        orientation.rotateLocalZ((rotateZ));
        //orientation.rotateLocalZ();
        return mat;
    }
 
     public void updateShapes(){
         if(doDynamicUpdates){
             mutateShapes();
             
             glBindBuffer(GL_ARRAY_BUFFER, vbo);
             glBufferSubData(GL_ARRAY_BUFFER, 0, vertexList);
             glBindBuffer(GL_ARRAY_BUFFER, colorvbo);
             glBufferSubData(GL_ARRAY_BUFFER, 0, colorList);
             glBindBuffer(GL_ARRAY_BUFFER, normalvbo);
             glBufferSubData(GL_ARRAY_BUFFER, 0, normalList);
         }
     }
 
     public static void main(String[]args){new Main();}
 }