import './CustomGraphics.css';

export default function CustomGraphics({ variant = 'dashboard', className = '' }) {
  return (
    <div className={`custom-graphics custom-graphics-${variant} ${className}`.trim()} aria-hidden="true">
      <svg className="custom-graphics-svg" viewBox="0 0 1200 800" preserveAspectRatio="none">
        <defs>
          <linearGradient id="gridGlow" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="rgba(94, 234, 212, 0.95)" />
            <stop offset="50%" stopColor="rgba(56, 189, 248, 0.85)" />
            <stop offset="100%" stopColor="rgba(245, 158, 11, 0.65)" />
          </linearGradient>
          <radialGradient id="pulseCore" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stopColor="rgba(125, 211, 252, 0.85)" />
            <stop offset="100%" stopColor="rgba(125, 211, 252, 0)" />
          </radialGradient>
        </defs>

        <g className="cg-grid">
          {Array.from({ length: 18 }).map((_, index) => (
            <line key={`v-${index}`} x1={index * 70} y1="0" x2={index * 70} y2="800" />
          ))}
          {Array.from({ length: 12 }).map((_, index) => (
            <line key={`h-${index}`} x1="0" y1={index * 70} x2="1200" y2={index * 70} />
          ))}
        </g>

        <path
          className="cg-wave cg-wave-primary"
          d="M-40 540 C 120 430, 250 650, 420 520 S 760 360, 930 470 S 1180 620, 1280 500"
        />
        <path
          className="cg-wave cg-wave-secondary"
          d="M-20 240 C 140 180, 280 340, 420 260 S 760 120, 920 250 S 1160 330, 1260 210"
        />

        <g className="cg-nodes">
          <circle className="cg-pulse" cx="220" cy="250" r="88" fill="url(#pulseCore)" />
          <circle className="cg-pulse" cx="820" cy="470" r="110" fill="url(#pulseCore)" />
          <circle className="cg-node" cx="220" cy="250" r="10" />
          <circle className="cg-node" cx="420" cy="260" r="10" />
          <circle className="cg-node" cx="620" cy="190" r="10" />
          <circle className="cg-node" cx="820" cy="470" r="10" />
          <circle className="cg-node" cx="1030" cy="236" r="10" />
        </g>

        <g className="cg-panels">
          <path d="M62 88 L290 88 L334 130 L334 270 L62 270 Z" />
          <path d="M858 470 L1118 470 L1148 500 L1148 688 L858 688 Z" />
        </g>
      </svg>

      <div className="custom-graphics-overlay custom-graphics-overlay-a" />
      <div className="custom-graphics-overlay custom-graphics-overlay-b" />
    </div>
  );
}
