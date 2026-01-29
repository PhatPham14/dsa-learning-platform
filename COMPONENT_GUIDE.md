# DSA Learning Platform - UI Component Guide

## Color System

### Primary Colors
- **Primary Blue**: #3b82f6 - Main action color, buttons, links
- **Primary Blue Dark**: #1e40af - Hover states, darker accents
- **Primary Blue Light**: #dbeafe - Backgrounds, subtle highlights

### Secondary Colors
- **Secondary Purple**: #8b5cf6 - Secondary actions, alternative CTAs
- **Success Green**: #10b981 - Positive actions, confirmations
- **Danger Red**: #ef4444 - Destructive actions, errors
- **Warning Orange**: #f59e0b - Warnings, important notices

### Neutral Colors
- **Dark**: #1f2937 - Headings, strong text
- **Text**: #374151 - Body text (default)
- **Muted**: #6b7280 - Secondary text, hints
- **Light**: #f3f4f6 - Light backgrounds
- **Border**: #e5e7eb - Dividers, borders

## Typography

### Heading Hierarchy
```
H1: 2.25rem (36px) - Page titles
H2: 1.875rem (30px) - Section headings
H3: 1.5rem (24px) - Subsection headings
H4+: Standard sizes - Content headings
```

### Font Stack
```
System UI Font (-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', etc.)
Monospace (Monaco, Courier New) - Code blocks
```

## Components

### Buttons

#### Primary Button
```
Background: var(--primary)
Color: white
Padding: 0.75rem 1.5rem
Border Radius: 0.5rem
Hover: Background darkens, elevation effect
```

#### Secondary Button
```
Background: var(--secondary)
Color: white
Padding: 0.75rem 1.5rem
Border Radius: 0.5rem
Hover: Lighter shade, elevation
```

#### Outline Button
```
Border: 2px solid var(--primary)
Color: var(--primary)
Background: transparent
Hover: Light blue background
```

### Forms

#### Input Fields
```
Width: 100%
Padding: 0.75rem
Border: 1px solid var(--border)
Border Radius: 0.5rem
Focus: Primary color border + light blue shadow
```

#### Textarea
```
Font: Monospace (Monaco, Courier New)
Min Height: 120px (or specified)
Resize: vertical
```

### Cards

#### Standard Card
```
Background: white
Padding: 1.5rem
Border Radius: 0.75rem
Shadow: Subtle shadow on normal, larger on hover
Transition: Smooth elevation and transform effects
```

#### Card Header
```
Displays: Flex layout with space-between
Border-bottom: 1px separator
Padding-bottom: 1rem
```

#### Card Footer
```
Border-top: 1px separator
Margin-top: auto
Display: Flex for button alignment
```

### Alerts

#### Success Alert
```
Background: #d1fae5 (light green)
Border-left: 4px solid #10b981 (green)
Color: #065f46 (dark green)
```

#### Danger Alert
```
Background: #fee2e2 (light red)
Border-left: 4px solid #ef4444 (red)
Color: #7f1d1d (dark red)
```

#### Warning Alert
```
Background: #fef3c7 (light orange)
Border-left: 4px solid #f59e0b (orange)
Color: #78350f (dark orange)
```

#### Info Alert
```
Background: var(--primary-light) (light blue)
Border-left: 4px solid var(--primary) (blue)
Color: #1e3a8a (dark blue)
```

### Badges

```
Display: Inline-block
Padding: 0.25rem 0.75rem
Border-radius: 9999px (pill shape)
Font-size: 0.875rem
Font-weight: 600
```

## Layout Patterns

### Hero Section
```
Background: Linear gradient (primary to secondary)
Color: white
Padding: 3rem 2rem
Text-align: center
Display: Flex column with centered content
```

### Grid System
```
.grid-2: 2 columns on desktop, 1 on mobile
.grid-3: 3 columns on desktop, 1 on mobile
Gap: 1.5rem
Uses: auto-fit, minmax() for responsiveness
```

### Navigation
```
Display: Flex
Justify-content: space-between
Align-items: center
Gap: 1rem
Padding: 1rem 2rem
Position: sticky, top: 0
z-index: 100
```

## Spacing Scale

```
0.25rem (4px)  - mt-1, mb-1
0.5rem (8px)   - mt-2, mb-2
0.75rem (12px) - mt-3, mb-3
1rem (16px)    - mt-4, mb-4 (default padding)
1.25rem (20px) - mt-5, mb-5
```

## Responsive Breakpoints

```
Mobile-first approach
Default: Single column, stacked
≤768px: Full screen width, responsive grids
>768px: Desktop layout, 2-3 column grids
```

## Shadow System

```
--shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1)
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1)
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1)
```

## Transition Effects

```
Default: all 0.3s ease
Used on:
  - Buttons (color, shadow, transform)
  - Cards (shadow, transform)
  - Links (color)
  - Forms (border, shadow)
```

## Icon Library

Using **lucide-react** with 290+ consistent icons:

### Common Icons Used
- `BookOpen` - Courses, education
- `Users` - Students, people
- `Zap` - Speed, energy, features
- `Award` - Achievements, certificates
- `LogIn` / `LogOut` - Authentication
- `UserPlus` - Registration, signup
- `ShoppingCart` - Purchase, checkout
- `Code2` - Code submissions
- `Send` - Submit, send
- `CheckCircle` - Success, completed
- `AlertCircle` - Errors, warnings
- `DollarSign` - Price, payment
- `TrendingUp` - Progress, growth
- `Clock` - Time, duration

## Accessibility Features

- Semantic HTML (form labels, inputs)
- Proper heading hierarchy
- ARIA labels where needed
- Color contrast compliance (WCAG AA)
- Keyboard navigation support
- Focus indicators on interactive elements

## Performance Optimizations

- CSS variables for easy theming
- Minimal CSS without frameworks
- SVG icons (lightweight via lucide-react)
- Smooth transitions (GPU accelerated)
- Optimized grid layouts

## Custom Utility Classes

```
.text-center    - Center text
.text-bold      - Bold font
.text-muted     - Secondary color text
.text-success   - Success color
.text-danger    - Danger color
.text-primary   - Primary color
.p-1/2/3/4      - Padding utility
.mt/mb/mt/mb-1-5 - Margin utilities
.card           - Card styling
.badge          - Badge styling
.alert          - Alert base styles
.grid/grid-2/3  - Grid layouts
```
