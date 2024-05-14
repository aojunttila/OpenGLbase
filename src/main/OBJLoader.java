
/*
* The MIT License (MIT)
*
* Copyright (c) 2014 Matthew 'siD' Van der Bijl
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*
*/
package main;

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.joml.*;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.GL_SHININESS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

/**
 * OBJloader class. Loads in Wavefront .obj file in to the program.
 *
 * @author Matthew Van der Bijl
 */
public class OBJLoader extends Object {

    /**
     * Constructs a new <code>OBJLoader</code>.
     */
    public OBJLoader() {
        super();
    }

    /**
     * @param file the file to be loaded
     * @return the loaded <code>Obj</code>
     * @throws java.io.FileNotFoundException thrown if the Obj file is not found
     */
    public ObjObject loadModel(File file) throws FileNotFoundException {
        return this.loadModel(new Scanner(file));
    }

    /**
     * @param stream the stream to be loaded
     * @return the loaded <code>Obj</code>
     */
    public ObjObject loadModel(InputStream stream) {
        return this.loadModel(new Scanner(stream));
    }

    /**
     * @param sc the <code>Obj</code> to be loaded
     * @return the loaded <code>Obj</code>
     */
    public ObjObject loadModel(Scanner sc) {
        ObjObject model = new ObjObject();
        while (sc.hasNextLine()) {
            String ln = sc.nextLine();
            if (ln == null || ln.equals("") || ln.startsWith("#")) {
            } else {
                String[] split = ln.split(" ");
                switch (split[0]) {
                    case "v":
                        model.getVertices().add(new Vector3f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2]),
                                Float.parseFloat(split[3])
                        ));
                        break;
                    case "vn":
                        model.getNormals().add(new Vector3f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2]),
                                Float.parseFloat(split[3])
                        ));
                        break;
                    case "vt":
                        model.getTextureCoordinates().add(new Vector2f(
                                Float.parseFloat(split[1]),
                                Float.parseFloat(split[2])
                        ));
                        break;
                    case "f":
                        String reg="/";
                        if(split[1].indexOf("//")!=-1){
                            reg="//";
                        }
                        if(split.length==4){
                            if(split[1].split(reg).length==3){
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[0]),
                                        Integer.parseInt(split[2].split(reg)[0]),
                                        Integer.parseInt(split[3].split(reg)[0])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[1]),
                                        Integer.parseInt(split[2].split(reg)[1]),
                                        Integer.parseInt(split[3].split(reg)[1])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[2]),
                                        Integer.parseInt(split[2].split(reg)[2]),
                                        Integer.parseInt(split[3].split(reg)[2])
                                    }
                                ));
                            }
                            if(split[1].split(reg).length==2){
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[0]),
                                        Integer.parseInt(split[2].split(reg)[0]),
                                        Integer.parseInt(split[3].split(reg)[0])
                                    },
                                    null,
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[1]),
                                        Integer.parseInt(split[2].split(reg)[1]),
                                        Integer.parseInt(split[3].split(reg)[1])
                                    }
                                ));
                            }
                            if(split[1].split(reg).length==1){
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1]),
                                        Integer.parseInt(split[2]),
                                        Integer.parseInt(split[3])
                                    },null,null
                                ));
                            }
                        }else if(split.length==5){
                            if(split[1].split(reg).length==3){
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[0]),
                                        Integer.parseInt(split[2].split(reg)[0]),
                                        Integer.parseInt(split[3].split(reg)[0])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[1]),
                                        Integer.parseInt(split[2].split(reg)[1]),
                                        Integer.parseInt(split[3].split(reg)[1])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[2]),
                                        Integer.parseInt(split[2].split(reg)[2]),
                                        Integer.parseInt(split[3].split(reg)[2])
                                    }
                                ));
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[0]),
                                        Integer.parseInt(split[4].split(reg)[0]),
                                        Integer.parseInt(split[3].split(reg)[0])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[1]),
                                        Integer.parseInt(split[4].split(reg)[1]),
                                        Integer.parseInt(split[3].split(reg)[1])
                                    },
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[2]),
                                        Integer.parseInt(split[4].split(reg)[2]),
                                        Integer.parseInt(split[3].split(reg)[2])
                                    }
                                ));
                            }
                            if(split[1].split(reg).length==2){
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[0]),
                                        Integer.parseInt(split[2].split(reg)[0]),
                                        Integer.parseInt(split[3].split(reg)[0])
                                    },
                                    null,
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[1]),
                                        Integer.parseInt(split[2].split(reg)[1]),
                                        Integer.parseInt(split[3].split(reg)[1])
                                    }
                                ));
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[0]),
                                        Integer.parseInt(split[4].split(reg)[0]),
                                        Integer.parseInt(split[3].split(reg)[0])
                                    },
                                    null,
                                    new int[]{
                                        Integer.parseInt(split[1].split(reg)[1]),
                                        Integer.parseInt(split[4].split(reg)[1]),
                                        Integer.parseInt(split[3].split(reg)[1])
                                    }
                                ));
                            }
                            if(split[1].split(reg).length==1){
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1]),
                                        Integer.parseInt(split[2]),
                                        Integer.parseInt(split[3])
                                    },null,null
                                ));
                                model.getFaces().add(new ObjObject.Face(
                                    new int[]{
                                        Integer.parseInt(split[1]),
                                        Integer.parseInt(split[4]),
                                        Integer.parseInt(split[3])
                                    },null,null
                                ));
                            }
                        }
                        break;
                    case "s":
                        model.setSmoothShadingEnabled(!ln.contains("off"));
                        break;
                    default:
                        //System.err.println("[OBJ] Unknown Line: " + ln);
                }
            }
        }
        sc.close();
        return model;
    }
}