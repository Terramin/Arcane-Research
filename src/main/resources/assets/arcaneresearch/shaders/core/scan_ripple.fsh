#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Progress;
uniform vec2 ScreenSize;

in vec2 vLocal;
in vec2 vTex;

out vec4 fragColor;

const float HEX_Y_STRETCH = 1.026;
const float APOTHEM = 0.8660254;
const float END = 0.85;
const float WAVE_WIDTH = 0.45;
const float WAVE_STRENGTH = 0.16;
const float SWIRL = 0.25;
const float ALPHA_GAIN = 1.8;

float hexRadius(vec2 p) {
    vec2 q = abs(p) * vec2(1.0, HEX_Y_STRETCH);
    return max(dot(q, vec2(0.8660254, 0.5)), q.y) / APOTHEM;
}

void main() {
    float t = clamp(Progress, 0.0, END);
    float k = t / END;

    float rHex = hexRadius(vLocal);

    float mask = 1.0 - smoothstep(0.96, 1.01, rHex);

    float r = length(vLocal);
    vec2 dir = r > 1.0e-4 ? vLocal / r : vec2(0.0, 1.0);
    vec2 tangent = vec2(-dir.y, dir.x);
    vec2 bent = normalize(dir + tangent * SWIRL);

    float d = t - rHex;
    float wave = exp(-d * d / (2.0 * WAVE_WIDTH * WAVE_WIDTH)) * pow(1.0 - k, 2.0);

    float amp = wave * WAVE_STRENGTH;
    vec2 suv = gl_FragCoord.xy / ScreenSize - bent * amp;
    vec3 bg = texture(Sampler0, suv).rgb;

    vec4 scr = texture(Sampler1, vTex);
    vec3 col = bg * 0.65 + scr.rgb * 0.6;
    float alpha = clamp(wave * ALPHA_GAIN, 0.0, 1.0) * mask;
    alpha *= clamp(scr.a * 1.5, 0.1, 1.0);
    fragColor = vec4(col, min(alpha, 0.8));
}
