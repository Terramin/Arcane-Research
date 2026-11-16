#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GlitchTime;
uniform float GlitchAmount;

out vec4 vertexColor;
out vec2 texCoord0;
out float vBand;
out float vSlice;

void main() {
    // hash per horizontal slice; slices scroll upward over time
    float slice = floor(Position.y * 9.0 - GlitchTime * 5.0);
    float h = fract(sin(slice * 91.1734) * 43758.5453);

    // only some slices shear, gate scales with the charge progress
    float gate = step(1.0 - GlitchAmount * 0.8, h);
    vec3 pos = Position;
    pos.x += gate * (h - 0.5) * 0.55 * GlitchAmount;
    pos.z += gate * (fract(h * 7.31) - 0.5) * 0.35 * GlitchAmount;
    pos.y += gate * (fract(h * 3.77) - 0.5) * 0.08 * GlitchAmount;

    // push the glitch shell out along normals so it hugs the model surface
    // without z-fighting
    pos += Normal * 0.006;

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    vBand = fract(Position.y * 2.5 - GlitchTime * 3.0);
    vSlice = h;
}
