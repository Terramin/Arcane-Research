#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;

out vec4 fragColor;

void main() {
    vec3 blurred = vec3(0.0);
    float totalAlpha = 0.0;
    float totalWeight = 0.0;
    float safeRadius = max(Radius, 1.0);
    for (float r = -Radius; r <= Radius; r += 1.0) {
        float w = 1.0 - abs(r / safeRadius);
        vec4 s = texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
        blurred += s.rgb * w;
        totalAlpha += s.a * w;
        totalWeight += w;
    }
    fragColor = vec4(blurred / totalWeight, totalAlpha / totalWeight);
}
