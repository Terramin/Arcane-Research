#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform vec2 ScreenSize;

in vec2 vLocal;
in vec2 vDir;
in float vScale;

out vec4 fragColor;

const float CYCLE = 1.0;
const float MAX_R = 2.0;
const float WAVE_WIDTH = 0.45;
const float WAVE_STRENGTH = 1.6;
const float SWIRL = 0.22;
const float ALPHA_GAIN = 3.0;

void main() {
    float r = length(vLocal);

    vec2 dir = vDir * 2.0 - 1.0;
    if (dot(dir, dir) < 1.0e-4) {
        dir = vec2(0.0, 1.0);
    }
    dir = normalize(dir);
    vec2 tangent = vec2(-dir.y, dir.x);
    vec2 bent = normalize(dir + tangent * SWIRL);

    float t = fract(Time / CYCLE);
    float d = t * MAX_R - r;
    float wave = exp(-d * d / (2.0 * WAVE_WIDTH * WAVE_WIDTH)) * pow(1.0 - t, 3.0);

    float amp = wave * WAVE_STRENGTH;
    vec2 offset = bent * (amp * vScale);
    vec2 suv = gl_FragCoord.xy / ScreenSize - offset;
    vec3 bg = texture(Sampler0, suv).rgb;

    float alpha = clamp(wave * ALPHA_GAIN, 0.0, 1.0);
    fragColor = vec4(bg, alpha);
}
