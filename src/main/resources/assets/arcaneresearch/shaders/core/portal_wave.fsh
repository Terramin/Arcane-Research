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

// --- постоянная линза "чёрной дыры" в центре портала ---
// радиус ядра линзы (мировые единицы; искажение затухает задолго до рамки)
const float LENS_RADIUS = 0.5;
// сила постоянного искажения на оси (в мировых единицах смещения сэмпла)
const float LENS_STRENGTH = 2.4;
// затемнение "горизонта событий" в самом центре
const float HORIZON_TINT = 0.45;
const float HORIZON_RADIUS = 0.35;

// --- кольцевая волна: раз в CYCLE секунд от центра расходится кольцо ---
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

    // постоянная линза: гауссов хребет в центре — фон "растягивается" наружу
    float lens = exp(-r * r / (2.0 * LENS_RADIUS * LENS_RADIUS));
    // волна: искажение только вблизи гребня, затухает по мере расширения
    float t = fract(Time / CYCLE);
    float d = t * MAX_R - r;
    float wave = exp(-d * d / (2.0 * WAVE_WIDTH * WAVE_WIDTH)) * pow(1.0 - t, 3.0);

    float amp = lens * LENS_STRENGTH + wave * WAVE_STRENGTH;
    // смещение сэмпла фона: в NDC, с перспективным масштабом vScale
    vec2 offset = bent * (amp * vScale);
    vec2 suv = gl_FragCoord.xy / ScreenSize - offset;
    vec3 bg = texture(Sampler0, suv).rgb;

    // "горизонт событий" — тёмное ядро в центре линзы
    float horizon = exp(-r * r / (2.0 * HORIZON_RADIUS * HORIZON_RADIUS));
    bg *= 1.0 - HORIZON_TINT * horizon;

    // ядро линзы и гребень волны непрозрачны: показывается ИСКАЖЁННЫЙ фон
    // (изгиб пространства); к краям квада альфа уходит в 0 — полотна не видно
    float alpha = clamp((lens + wave) * ALPHA_GAIN, 0.0, 1.0);
    fragColor = vec4(bg, alpha);
}
