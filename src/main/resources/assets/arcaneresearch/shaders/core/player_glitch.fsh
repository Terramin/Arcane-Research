#version 150

in vec4 vertexColor;
in vec2 texCoord0;
in float vBand;
in float vSlice;

uniform vec4 ColorModulator;
uniform float GlitchTime;
uniform float GlitchAmount;

out vec4 fragColor;

void main() {
    float t = GlitchTime;

    // flickering scanline bands crawling over the character
    float band = step(0.90, fract(vBand * 9.0 + t * 4.0));

    // hard magenta / cyan channel grind, hue flips per slice over time
    vec3 mag = vec3(1.00, 0.10, 0.85);
    vec3 cyn = vec3(0.15, 0.90, 1.00);
    float mixv = fract(t * 0.9 + vSlice * 13.7);
    vec3 col = mix(mag, cyn, step(0.5, mixv));

    // occasional white "static" flash on active slices
    float flash = 0.55 + 0.45 * sin(t * 42.0 + vSlice * 97.0);
    col = mix(col, vec3(1.0), band * flash * 0.35);

    // broken alpha: slices pulse in and out, some cut away completely
    float cut = step(0.12, vSlice);
    float alpha = GlitchAmount * cut * (0.30 + 0.55 * band) * flash;
    alpha = clamp(alpha, 0.0, 0.9);

    fragColor = vec4(col, alpha) * ColorModulator;
}
