#version 150

// ВАЖНО: имя именно Sampler0 — ванильный VertexBuffer биндит сэмплеры
// по именам "Sampler<i>" из RenderSystem.getShaderTexture(i).
uniform sampler2D Sampler0;
uniform float Time;
uniform vec2 ScreenSize;

in vec2 vLocal;
in vec2 vDir;
in float vScale;

out vec4 fragColor;

// --- кольцевая волна: раз в CYCLE секунд от центра расходится кольцо ---
// всё искажение существует ТОЛЬКО на самой волне; вне кольца полотно прозрачно
const float CYCLE = 1.0;
// радиус, до которого волна расходится; затухает раньше кромки квада
const float MAX_R = 2.0;
// полуширина кольца по гауссу: искажает только КРАЙ волны, не весь диск
const float WAVE_WIDTH = 0.45;
// амплитуда искажения волны на гребне (в мировых единицах)
const float WAVE_STRENGTH = 1.6;
// доля тангенциального (закручивающего) сдвига — "черно дырный" характер
const float SWIRL = 0.22;
// непрозрачность там, где искажение заметно
const float ALPHA_GAIN = 3.0;

void main() {
    float r = length(vLocal);

    // радиальное направление на экране (упаковано CPU-стороной с учётом перспективы)
    vec2 dir = vDir * 2.0 - 1.0;
    if (dot(dir, dir) < 1.0e-4) {
        dir = vec2(0.0, 1.0);
    }
    dir = normalize(dir);
    // лёгкое закручивание направления — "линзирование" вокруг масс
    vec2 tangent = vec2(-dir.y, dir.x);
    vec2 bent = normalize(dir + tangent * SWIRL);

    // волна: искажение только вблизи гребня, затухает по мере расширения
    float t = fract(Time / CYCLE);
    float d = t * MAX_R - r;
    float wave = exp(-d * d / (2.0 * WAVE_WIDTH * WAVE_WIDTH)) * pow(1.0 - t, 3.0);

    float amp = wave * WAVE_STRENGTH;
    // смещение сэмпла фона: в NDC, с перспективным масштабом vScale
    vec2 offset = bent * (amp * vScale);
    vec2 suv = gl_FragCoord.xy / ScreenSize - offset;
    vec3 bg = texture(Sampler0, suv).rgb;

    // гребень волны непрозрачен: показывается ИСКАЖЁННЫЙ фон (изгиб
    // пространства); снаружи кольца альфа = 0 — полотна не видно
    float alpha = clamp(wave * ALPHA_GAIN, 0.0, 1.0);
    fragColor = vec4(bg, alpha);
}
