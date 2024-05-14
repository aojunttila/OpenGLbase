/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package main;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.*;
import static org.lwjgl.opengl.ARBVertexShader.GL_VERTEX_SHADER_ARB;
import static org.lwjgl.opengl.ARBVertexShader.glGetAttribLocationARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

import java.lang.Math;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

/**
 * Simple "free fly" camera demo.
 * 
 * @author Kai Burjack
 */
public class OpenGLApp {
    private long window;
    private int width = 1200;
    private int height = 800;
    private int mouseX, mouseY;
    private boolean viewing;

    private final Matrix4f mat = new Matrix4f();
    private final Quaternionf orientation = new Quaternionf();
    private final Vector3f position = new Vector3f(0, 2, 5).negate();
    private boolean[] keyDown = new boolean[GLFW_KEY_LAST + 1];
    
    double pitch=0;
    double yaw=0;int location;
    float sensitivity=0.1f;
    private int grid;
    private int gridProgram;
    private int gridProgramMatLocation;

    private void run() {
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        window = glfwCreateWindow(width, height, "Hello, free fly camera!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        glfwSetKeyCallback(window, (long window1, int key, int scancode, int action, int mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window1, true);
            } else if (key >= 0 && key <= GLFW_KEY_LAST) {
                keyDown[key] = action == GLFW_PRESS || action == GLFW_REPEAT;
            }
        });
        glfwSetFramebufferSizeCallback(window, (long window3, int w, int h) -> {
            if (w > 0 && h > 0) {
                width = w;
                height = h;
            }
        });

        glfwSetCursorPosCallback(window, (long window2, double xpos, double ypos) -> {
            float deltaX = (float) (xpos - mouseX);
            float deltaY = (float) (ypos - mouseY);

            // Apply rotation to the orientation
            //orientation.rotateLocalY(deltaX*0.01f);
            //orientation.rotateLocalX(deltaY*0.01f);

            mouseX = (int) xpos;
            mouseY = (int) ypos;

            double prevPitch=pitch;
            float rotateY=deltaY*(0.01f*sensitivity);
            float rotateX=deltaX*(0.01f*sensitivity);
            yaw+=rotateX;
            pitch+=rotateY;
            if(pitch>1.5){
                pitch=1.5;rotateY=(float)(pitch-prevPitch);    
            }
            if(pitch<-1.5){
                pitch=-1.5;rotateY=(float)(pitch-prevPitch);    
            }
            orientation.rotateY(rotateX);

            // Apply pitch (up-down rotation) around the local X-axis
            orientation.rotateLocalX(rotateY);

        });
        glfwSetMouseButtonCallback(window, (long window4, int button, int action, int mods) -> {
            if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                viewing = true;
            } else {
                viewing = false;
            }
        });


        glfwSetCursorEnterCallback(window, (windowHandle, entered) -> {
            if (entered) {
                glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            } else {
                glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
        });
        try (MemoryStack stack = stackPush()) {
            IntBuffer framebufferSize = stack.mallocInt(2);
            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
            width = framebufferSize.get(0);
            height = framebufferSize.get(1);
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GLCapabilities caps = GL.createCapabilities();
        if (!caps.GL_ARB_shader_objects)
            throw new UnsupportedOperationException("ARB_shader_objects unsupported");
        if (!caps.GL_ARB_vertex_shader)
            throw new UnsupportedOperationException("ARB_vertex_shader unsupported");
        if (!caps.GL_ARB_fragment_shader)
            throw new UnsupportedOperationException("ARB_fragment_shader unsupported");
        glClearColor(0.7f, 0.8f, 0.9f, 1);
        glEnable(GL_BLEND);
        glEnable(GL_MULTISAMPLE);   // enable MSAA
        glEnable(GL_DEPTH_TEST);    // enable depth testing
        //glEnable(GL_CULL_FACE);     // enable culling
        //glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        createGridProgram();
        //createGrid();
        
        glfwShowWindow(window);
        long lastTime = System.nanoTime();
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glViewport(0, 0, width, height);
            glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
            glUseProgramObjectARB(gridProgram);
            long thisTime = System.nanoTime();
            float dt = (thisTime - lastTime) * 1E-9f;
            lastTime = thisTime;
            try (MemoryStack stack = stackPush()) {
                glUniformMatrix4fvARB(gridProgramMatLocation, false, updateMatrices(dt).get(stack.mallocFloat(16)));
            }
            GL20.glUseProgram(gridProgram);
            final int RAND_AMOUNT = 50;
            FloatBuffer buffer = BufferUtils.createFloatBuffer(RAND_AMOUNT);
            float[] array = new float[RAND_AMOUNT];
            for(int i = 0; i < colorList.length; i++) {
                buffer.put(colorList[i]);
            }
            buffer.rewind();
            GL20.glUniform1fv(location, buffer);
            
            glBindVertexArray(1);
            glClear(GL_COLOR_BUFFER_BIT);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);
            
            //glCallList(grid);
            glfwSwapBuffers(window);
        }
    }

    private Matrix4f updateMatrices(float dt) {
        float rotateZ = 0f;
        float speed = 0.1f;
        if(keyDown[GLFW_KEY_LEFT_CONTROL]){speed = 0.2f;}
        if(keyDown[GLFW_KEY_Q]){rotateZ-=1f;}
        if(keyDown[GLFW_KEY_E]){rotateZ+=1f;}
        if(keyDown[GLFW_KEY_W]){position.add(new Vector3f(speed*(float)Math.sin(-yaw),0f,speed*(float)Math.cos(-yaw)));}
        if(keyDown[GLFW_KEY_S]){position.add(new Vector3f(-speed*(float)Math.sin(-yaw),0f,-speed*(float)Math.cos(-yaw)));}
        if(keyDown[GLFW_KEY_A]){position.add(new Vector3f(-speed*(float)Math.sin(-yaw-Math.PI/2),0f,-speed*(float)Math.cos(-yaw-Math.PI/2)));}
        if(keyDown[GLFW_KEY_D]){position.add(new Vector3f(speed*(float)Math.sin(-yaw-Math.PI/2),0f,speed*(float)Math.cos(-yaw-Math.PI/2)));}
        if(keyDown[GLFW_KEY_SPACE]&&touchingGround){Yvelocity-=0.1;}
    
        runPhysics();
    
        orientation.rotateLocalZ(rotateZ * dt * speed);
        return mat.setPerspective((float) Math.toRadians(60), (float) width / height, 0.1f, 1000.0f)
                  .rotate(orientation)
                  .translate(position);
    }

    private void createGridProgram() {
        gridProgram = glCreateProgramObjectARB();
        int vs = glCreateShaderObjectARB(GL_VERTEX_SHADER_ARB);
        glShaderSourceARB(vs, 
              "#version 110\n" +
                "attribute vec3 vertexInfo;\n"+
                "//int colorIndex = int(vertexInfo);\n"+
                "uniform mat4 viewProjMatrix;\n" +
                "varying vec4 wp;\n" +
                "uniform float colors[50];\n"+
                "varying vec3 vertexColor;\n" +
                "int increment = 0;\n" +
                "int vertexCount = 0;\n"+
                "//vertexCount = 0;\n"+
                "void main() {\n" +
                "  increment = 0;\n"+
                "  vertexCount++;\n"+
                "  wp = gl_Vertex;\n" +
                "  gl_Position = viewProjMatrix * gl_Vertex;\n" +
                "  vertexColor = vec3(wp.x,0,0);\n" +
                "}");
        glCompileShaderARB(vs);
        glAttachObjectARB(gridProgram, vs);
        int fs = glCreateShaderObjectARB(GL_FRAGMENT_SHADER_ARB);
        System.out.println("1 "+glGetError());

        glShaderSourceARB(fs,
                "#version 110\n" +
                "//precision highp float;\n"+
                "//out vec4 FragColor;\n" +
                "varying vec4 wp;\n"+
                "varying vec3 vertexColor\n;"+
                "void main(){\n"+
                "   //FragColor = vec4(0.0f, 0.5f, 0.2f, 1.0f);\n" +
                "   gl_FragColor = vec4(vertexColor, 1.0f);\n"+
                "}");
        glCompileShader(fs);
        glAttachShader(gridProgram, fs);
        System.out.println("2 "+glGetError());
        glBindAttribLocation(gridProgram, 1, "vertexInfo");
        //glBindAttribLocation(gridProgram, 0, "vertexColor");
        glLinkProgram(gridProgram);
        System.out.println("3 "+glGetError());
        //System.out.println(glGetProgramInfoLog(gridProgram));
        gridProgramMatLocation = glGetUniformLocationARB(gridProgram, "viewProjMatrix");
        System.out.println(gridProgramMatLocation);
        //System.out.println(glGetUniformLocation(gridProgram, "vertexColor"));
        location = GL20.glGetUniformLocation(gridProgram, "colors");
        System.out.println(location);
        System.out.println("3.1 "+glGetError());

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexList, GL15.GL_STATIC_DRAW);
        System.out.println("4 "+glGetError());

        //int vertexIndexAttribLocation = glGetAttribLocation(gridProgram, "vertexIndex");
        //System.out.println("Vertex index attribute location: " + vertexIndexAttribLocation);
        System.out.println("4.1 "+glGetError());

        
        ByteBuffer colors = storeArrayInBuffer(colorList);
        int colorVBO = glGenBuffers();
        System.out.println("5 "+glGetError());
        /*
        int vertexIndexAttribLocation = glGetAttribLocation(gridProgram, "vertexInfo");

        // Bind the 'colorVBO' VBO for use
        glBindBuffer(GL_ARRAY_BUFFER, colorVBO);
        
        // Uploads VBO data (in this case, colors) to the GPU
        GL15.glBufferData(GL_ARRAY_BUFFER, colors, GL_STATIC_DRAW);

        // Specifies information about the format of the VBO (number of values per vertex, data type, etc.)
        glVertexAttribPointer(vertexIndexAttribLocation, 1, GL_FLOAT, false, 0, 0);
        
        // Enable vertex attribute array 1
        glEnableVertexAttribArray(vertexIndexAttribLocation);*/

        /*
        float[]indexData={0f,1f,2f,3f,4f,5f,6f,7f,8f,9f,10f};

        FloatBuffer indicesBuffer = BufferUtils.createFloatBuffer(indexData.length);
        indicesBuffer.put(indexData);
        indicesBuffer.flip();
        

        int indexBuffer = glGenBuffers();
        glBindVertexArray(indexBuffer);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        GL15.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);

        // Specify the format of the index data to OpenGL*/
        glVertexAttribPointer(1, 1, GL_FLOAT, false, 0, 0);

        //glBufferData(GL_ARRAY_BUFFER, createBuffer(vertices), GL_STATIC_DRAW);
        //glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 6 * Float.BYTES, 0);
        //glEnableVertexAttribArray(0);

        //glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        //glEnableVertexAttribArray(1);
        // Create vertex array object (VAO)
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        // Unbind VAO and VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    float[]vertices = new float[]{
        0.0f,  0.5f, 0.0f,  // top
       -0.5f, -0.5f, 0.0f,  // bottom-left
        0.5f, -0.5f, 0.0f   // bottom-right
    };
    private void createGrid() {
        grid = glGenLists(1);
        glNewList(grid, GL_COMPILE);
        glBegin(GL_TRIANGLES);
        glVertex4f(-1, 0, -1, 0);
        glVertex4f(-1, 0,  1, 0);
        glVertex4f( 0, 0,  0, 1);
        glVertex4f(-1, 0,  1, 0);
        glVertex4f( 1, 0,  1, 0);
        glVertex4f( 0, 0,  0, 1);
        glVertex4f( 1, 0,  1, 0);
        glVertex4f( 1, 0, -1, 0);
        glVertex4f( 0, 0,  0, 1);
        glVertex4f( 1, 0, -1, 0);
        glVertex4f(-1, 0, -1, 0);
        glVertex4f( 0, 0,  0, 1);

        glVertex3f( 2.0f, 2.5f, 2f);
        glVertex3f(0.5f, -0.5f,0f);
        glVertex3f( -2.5f, -2.5f,-2f);

        glVertex3f( -2.0f, 4.5f, 2f);
        glVertex3f(0.5f, -0.5f,0f);
        glVertex3f( 2.5f, -4.5f,-2f);
        
        glEnd();
        glEndList();

    }
    float[]vertexList={
        1.0f,1.0f,1.0f,
        0.0f,1.0f,0.5f,
        1.0f,0.0f,0.0f,
        1.0f,2.0f,1.0f,
        0.0f,2.0f,0.5f,
        1.0f,0.0f,0.0f,

    };
    float[]colorList={
        0.0f,1.0f,0.5f,
        0.0f,1.0f,0.5f,
        1.0f,0.0f,0.0f,
        0.0f,1.0f,0.5f,
        0.0f,1.0f,0.5f,
        1.0f,0.0f,0.0f,
    };
    float Yvelocity=0;
boolean touchingGround=true;
private void runPhysics(){
    position.y+=Yvelocity;
    
    if(position.y>-1.5f){
        position.y=-1.5f;
        Yvelocity=0;
        touchingGround=true;
    }else{touchingGround=false;}
    if(!touchingGround){
        Yvelocity+=0.006;
        if(Yvelocity>1){Yvelocity=1;}
    }
}

private FloatBuffer createBuffer(float[] data) {
    FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
    buffer.put(data);
    buffer.flip();
    return buffer;
}

public static ByteBuffer storeArrayInBuffer(float[] array) {
		
    //8 bytes (64-bits) in a double, multiplied by the number of values in the array
    ByteBuffer buffer = BufferUtils.createByteBuffer(array.length * 4);
    
    for(float i : array) {
        buffer.putFloat(i);
    }
    
    buffer.position(0);
    
    return buffer;
}

    public static void main(String[] args) {
        new OpenGLApp().run();
    }
}