#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform float WarpTime;     // absolute seconds, updated from Java every frame
uniform float WarpStrength; // 0..1 overall warp amount, eased on the Java side

out vec4 fragColor;

void main() {
    float aspect = InSize.x / max(InSize.y, 1.0);
    vec2 centered = texCoord - vec2(0.5);
    centered.x *= aspect;

    float r = length(centered);
    float ang = atan(centered.y, centered.x);
    vec2 dir = (r > 1e-5) ? (centered / r) : vec2(0.0);

    // spiral swirl, strongest half way out like the portal plane pull
    float spin = (1.0 + 0.22 * sin(WarpTime * 2.6)) * (1.0 - r) * r * 2.0;
    ang += spin * WarpStrength * (0.35 + 0.65 * r);

    // breathing radial pull while being sucked in
    float pulse = sin(WarpTime * 3.1 + r * 14.0);
    float rr = r + 0.05 * WarpStrength * pulse;

    // traveling distortion wave, same trick as vanilla wobble
    float wave = 0.045 * WarpStrength * sin(WarpTime * 4.0 + r * 22.0 - ang * 2.0);
    rr += wave;

    vec2 warped = vec2(cos(ang), sin(ang)) * rr;
    warped.x /= aspect;
    vec2 uv = clamp(warped + vec2(0.5), vec2(0.0), vec2(1.0));

    // prismatic shimmer along the radial direction
    float chroma = 0.006 * WarpStrength * sin(WarpTime * 5.0 + r * 10.0);
    vec2 uvR = clamp(uv + dir * chroma, vec2(0.0), vec2(1.0));
    vec2 uvB = clamp(uv - dir * chroma, vec2(0.0), vec2(1.0));
    vec3 col = vec3(
        texture(DiffuseSampler, uvR).r,
        texture(DiffuseSampler, uv).g,
        texture(DiffuseSampler, uvB).b
    );

    // arcane vignette: the world darkens into a shimmering violet horizon
    float vign = 1.0 - smoothstep(0.35, 1.45, r);
    float ring = clamp(1.0 - abs(r - (0.55 + 0.08 * sin(WarpTime * 2.0))) * 4.0, 0.0, 1.0);
    float tintAmt = clamp(WarpStrength * (0.35 + 0.65 * ring) * (1.0 - vign * 0.7), 0.0, 1.0);
    col = mix(col, col * vec3(0.72, 0.5, 1.05) + vec3(0.08, 0.02, 0.16), tintAmt);

    col *= mix(1.0, vign, WarpStrength * 0.85);

    fragColor = vec4(col, 1.0);
}
