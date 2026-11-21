#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 vLocal;
out vec2 vDir;
out float vScale;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vLocal = UV0;
    vDir = Color.rg;
    vScale = Color.b * 2.0;
}
