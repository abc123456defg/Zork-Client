/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package cc.zork.utils.render.shader;

import net.minecraft.client.Minecraft;
import cc.zork.Zork;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class Shader {
    private int program;
    Minecraft mc = Minecraft.getMinecraft();

    private Map<String, Integer> uniformsMap;

    public Shader(final String fragmentShader) {
        int vertexShaderID, fragmentShaderID;

        try {
            final InputStream vertexStream = getClass().getResourceAsStream("/assets/minecraft/client/shaders/vertex.vsh");
            vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB);
            IOUtils.closeQuietly(vertexStream);

            final InputStream fragmentStream = getClass().getResourceAsStream("/assets/minecraft/client/shaders/fragment/" + fragmentShader);
            fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
            IOUtils.closeQuietly(fragmentStream);
        }catch(final Exception e) {
            e.printStackTrace();
            return;
        }

        if(vertexShaderID == 0 || fragmentShaderID == 0)
            return;

        program = ARBShaderObjects.glCreateProgramObjectARB();

        if(program == 0)
            return;

        ARBShaderObjects.glAttachObjectARB(program, vertexShaderID);
        ARBShaderObjects.glAttachObjectARB(program, fragmentShaderID);

        ARBShaderObjects.glLinkProgramARB(program);
        ARBShaderObjects.glValidateProgramARB(program);

        Zork.Companion.getLogger().info("[Shader] Successfully loaded: " + fragmentShader);
    }

    public void startShader() {
        GL11.glPushMatrix();
        GL20.glUseProgram(program);

        if(uniformsMap == null) {
            uniformsMap = new HashMap<>();
            setupUniforms();
        }

        updateUniforms();
    }

    public void stopShader() {
        GL20.glUseProgram(0);
        GL11.glPopMatrix();
    }

    public abstract void setupUniforms();

    public abstract void updateUniforms();

    private int createShader(String shaderSource, int shaderType) {
        int shader = 0;

        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if(shader == 0)
                return 0;

            ARBShaderObjects.glShaderSourceARB(shader, shaderSource);
            ARBShaderObjects.glCompileShaderARB(shader);

            if(ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

            return shader;
        }catch(final Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw e;

        }
    }

    private String getLogInfo(int i) {
        return ARBShaderObjects.glGetInfoLogARB(i, ARBShaderObjects.glGetObjectParameteriARB(i, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    public void setUniform(final String uniformName, final int location) {
        uniformsMap.put(uniformName, location);
    }

    public void setupUniform(final String uniformName) {
        setUniform(uniformName, GL20.glGetUniformLocation(program, uniformName));
    }

    public int getUniform(final String uniformName) {
        return uniformsMap.get(uniformName);
    }

    public int getProgramId() {
        return program;
    }
}