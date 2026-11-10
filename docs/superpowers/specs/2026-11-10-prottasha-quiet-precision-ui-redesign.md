# Prottasha Quiet Precision UI/UX Redesign

**Date:** 2026-11-10
**Status:** Approved visual direction; implementation pending
**Scope:** UI/UX and presentation only
**Reference device:** TECNO BG7, Android 13, 720×1612, 4 GB RAM

## Intent

Redesign Prottasha as a premium, polished, modern 2026 Android product without changing its established feature behavior, persistence, encryption, Firebase authority, or domain logic.

The current interface exposes too many sections with equal visual weight. Repeated outlined cards, long explanations, full-width buttons, small typography, and visible filters make complete features feel like a prototype. The redesign will preserve information while establishing hierarchy and progressive disclosure.

## Goals

- Premium, polished, modern presentation with a distinctive human character.
- Integrate the new Prottasha logo as a restrained brand signature.
- Make Sign In, Create Account, verification, reset, Profile, and onboarding feel first-class.
- Preserve useful dashboard information upfront without returning to clutter.
- Add a contribution-style habit consistency heatmap sourced from real events.
- Make all product copy plain, direct, specific, and human.
- Preserve accessibility, large text, rotation, denied/offline/error states, and BG7 performance.
- Reuse existing data and behavior; modify core code only where presentation cannot otherwise work.

## Non-goals

- No feature redesign, database schema migration, Firebase model change, rules change, or business-logic rewrite.
- No replacement of Java/XML with Compose or Kotlin.
- No light-theme requirement for this academic release.
- No decorative analytics, fabricated data, motivational feed, gamification currency, or social feed.
- No glassmorphism, blur-heavy surfaces, gradient blobs, neon glows, or generic bento-card wallpaper.
- No separate visual language for every feature.

## Selected direction: Quiet Precision

Quiet Precision combines:

- Deep neutral surfaces rather than pure black.
- Human Editorial typography for identity and high-emotion moments.
- Precise sans-serif interface typography for controls and information.
- Monospaced labels for dates, ranges, and metrics.
- Mint and periwinkle accents sampled from the logo, used selectively.
- One dominant visual/action per screen with supporting information organized beneath it.
- Thin separators and grouped rows instead of placing every section in a card.
- Restrained branded horizon arcs inspired by the logo, used only in hero moments.

## Brand asset

Current source: `prottasha.png` at repository root.

Produce Android-ready derivatives without redesigning the mark:

- Adaptive launcher foreground and solid background.
- Android 13 monochrome themed icon.
- White-only notification silhouette.
- Splash/auth mark with safe transparent padding.
- Optional horizontal wordmark for presentation material.

The full logo appears on splash and authentication screens. Main product screens use it sparingly; it must not become a repeated decorative watermark.

## Color system

Final values must pass contrast checks on-device.

| Token | Proposed value | Role |
|---|---:|---|
| Canvas | `#07090D` | Window/background |
| Base surface | `#0D1117` | Main screen plane |
| Tonal surface | `#151A22` | Grouped controls and selected regions |
| Raised surface | `#1A2029` | Dialogs, sheets, active controls |
| Divider/border | `#252D39` | Hairlines and explicit boundaries |
| Primary text | `#F5F7FA` | Main content |
| Secondary text | `#98A3B1` | Supporting content |
| Quiet text | `#6D7886` | Metadata only after contrast verification |
| Hope mint | `#43E0AB` | Primary actions, active state, positive intensity |
| Identity periwinkle | `#8B91FF` | Identity, reflection, current-day marker |
| Friction amber | `#F0BD66` | Warnings and Pause mode |
| Crisis red | `#FF8080` | Destructive and emergency semantics only |

Rules:

- No decorative page-level gradients.
- Logo-internal color variation may remain.
- Accent colors communicate role; they do not color entire cards.
- Primary actions use mint with dark foreground.
- Avoid multiple semantic accent colors in the same compact region.

## Typography

Bundle OFL font files locally; no runtime font download.

### Families

- **Newsreader 500/600:** screen display titles, identity statements, selected hero metrics, and editorial reflection moments.
- **IBM Plex Sans 400/500/600/700:** body copy, controls, forms, lists, navigation, dialogs, and dense content.
- **IBM Plex Mono 400/500:** dates, ranges, status labels, compact statistics, and chart legends.

### Scale

| Role | Size | Notes |
|---|---:|---|
| Display | 30–34sp | Newsreader, limited to one per screen |
| Headline | 24–26sp | Newsreader or Plex Sans by context |
| Section title | 18sp | Plex Sans 600 |
| Body | 15–16sp | Plex Sans 400 |
| Control/label | 13–14sp | Plex Sans 500/600 |
| Metadata | 11–12sp | Plex Mono or Plex Sans; never essential prose |

Serif is never used for text fields, buttons, navigation, settings rows, long explanations, or dense analytics.

## Shape, spacing, and elevation

- Screen horizontal padding: 20dp on phones.
- Spacing scale: 4, 8, 12, 16, 24, and 32dp.
- Hero radius: 20dp when a bounded hero is needed.
- Standard surface radius: 16dp.
- Control radius: 12–14dp.
- Chips: 8–10dp; avoid pills unless the content is a true compact status/filter.
- Minimum touch target: 48dp.
- Use tonal separation and 1dp dividers before elevation.
- Shadows appear only on modal/sheet layers; ordinary cards remain flat.

## Information hierarchy

Each top-level screen contains:

1. Clear display title and optional one-line context.
2. One primary hero/status/metric region.
3. Up to three supporting metric/control groups visible upfront.
4. Compact rows for repeated records.
5. Secondary configuration in tabs, sheets, dialogs, or detail screens.
6. One obvious primary action, contextual rather than always full-width.

The user selected **more information upfront**, so top-level dashboards remain informative. The redesign organizes information rather than hiding all depth.

## Navigation and app chrome

- Keep five labelled bottom destinations: Today, Habits, Guard, Insights, Circle.
- Use one consistent vector icon family and optical size.
- Active destination uses mint; inactive destinations use quiet neutral.
- Replace text-only Lifeline/Settings header buttons with accessible icon controls and labels/content descriptions.
- Main header should not repeat “Prottasha” on every screen; destination titles establish location.
- Back behavior and destination state remain unchanged.

## Screen design

### Authentication

- Use the logo once near the top with generous whitespace.
- Sign In title: “Sign in”. Subtitle: “Use your verified Prottasha account.”
- Keep autofill, paste, password manager, show-password, errors, progress, verification, and reset behavior unchanged.
- Use clean underlined or tonal fields; avoid a large form card.
- One filled primary button; account creation is a text/secondary action.
- Include one factual privacy statement at the bottom, not promotional claims.
- Create Account uses a visible step/progress label and scannable password requirements.

### Today

- Greeting/date and profile avatar at the top.
- Identity statement is the editorial hero with one logo-derived horizon arc.
- Show useful information upfront: consistency, mindful exits/follow-through, focus action, due habits, and active boundaries.
- Replace stacked identity/Horizon/habits/boundaries cards with hierarchy, rows, and compact metric surfaces.
- Include a 12-week contribution-style consistency heatmap.
- Heatmap must show visible summaries such as active days, longest run, and consistency; color is supplementary.

### Habits

- Use RecyclerView rows with strong title, concise schedule/progress metadata, one completion control, and overflow actions.
- Add action becomes a contextual floating/toolbar action instead of a full-width top button.
- Filters appear in a compact selector or bottom sheet; archived is not equal to primary habit types.
- Detail screen prioritizes current action, streak/consistency heatmap, history, then planning metadata.
- Editor uses grouped steps/sections with a sticky save action; advanced fields remain collapsed until selected.

### Guard

- Lead with one compact status line: “Protection is on” or a specific repair state.
- Use Rules, Schedule, and Health tabs; do not display the full health diagnostic card above every rule.
- Rules are compact rows with target icon, title, schedule/budget detail, mode label, enabled state, and overflow menu.
- Search is an icon/action; filters open contextually rather than occupying permanent vertical space.
- Add boundary is the primary contextual action.
- Permission/health screens remain detailed where repair decisions happen.

### Insights

- Keep Today/7 days/30 days as a compact segmented selector.
- Show one primary metric, heatmap/trend, and two or three useful observations upfront.
- Preserve more information through grouped metric sections and drill-down, not seven equal cards.
- Move reflections, trigger plans, success memories, and rehearsal into a clearly labelled “Journal & tools” library.
- Every chart retains an accessible textual summary.

### Circle

- Lead with real member presence and the closed-pod entry.
- Present Accountability partner, Support directory, and Immediate help as compact destinations.
- Keep emergency styling distinct but restrained.
- Move long privacy explanation into “What Circle can see” detail/sheet.
- Chat prioritizes messages, presence/typing, composer, report/block actions, and clear offline state.

### Profile

- Existing image selection is retained: encrypted local photo, preview, change, and remove.
- Make the avatar/photo the focal point with a camera/edit affordance.
- Clearly separate “Private profile” from “Circle profile”.
- Explain in one sentence that the local photo is not uploaded; Circle receives generated avatar fields only.
- Keep display name, pseudonym, avatar palette/style, loading, validation, and save behavior unchanged.

### Settings

- Use grouped settings rows with icons, title, optional one-line status, and chevron.
- Avoid rendering every row as a text-styled button.
- Place destructive/account actions in a separated section.
- Show current lock/permission/account status inline where useful.

### Focus and intervention

- Focus screen centers the timer and current state; secondary controls remain visually subordinate.
- Intervention overlay remains immediate and lightweight; Step Back is visually dominant.
- No decorative animation required for correctness.
- Reduced-motion mode renders final states immediately.

## Contribution-style consistency visual

- 7 rows by up to 12 weeks on Today; longer ranges may live in Habit detail/Insights.
- Cell intensity derives from persisted scheduled/completion data only.
- Empty/no-scheduled days remain neutral and are not treated as failure.
- Current day receives a periwinkle outline, not a fabricated completion color.
- Include a visible legend and text summary.
- Custom View exposes a concise TalkBack summary and an optional day-by-day accessible detail view.

## Product voice: normal, not AI-generated

### Rules

- Use familiar words and direct verbs.
- State what happened and what the user can do next.
- Prefer one short sentence over a headline plus motivational subtitle.
- Keep evidence/privacy/safety caveats only where they affect a decision.
- Use specific errors: operation + cause/repair when known.
- Empty states describe what is empty and provide a real action.

### Banned patterns

- “Your journey begins…”
- “Unlock your potential.”
- “Empower your future self.”
- “One meaningful step at a time.”
- “Mindful protection shield.”
- “Something went wrong on our end.”
- Repeated em-dash constructions and abstract reassurance.
- Overuse of “hope”, “intentional”, “meaningful”, “transform”, or “thrive”.

### Preferred examples

- “No habits are due today.”
- “Protection is on.”
- “Usage access is off. Turn it on to use Budget mode.”
- “Could not save the rule. Check your connection and try again.”
- “Photo stored locally on this device.”
- “3 members online.”

## Motion

- Fast feedback: 100–140ms.
- Standard transitions: 180–240ms.
- Emphasized hero/sheet motion: 300–360ms maximum.
- Use alpha, translation, and small scale changes; no springy card parade.
- No stagger animation for every dashboard item.
- Respect system animator scale and reduced-motion expectations.

## Accessibility

- Maintain 4.5:1 contrast for normal text.
- Do not encode completion, mode, warnings, or chart intensity by color alone.
- Keep 48dp targets and meaningful TalkBack order.
- Icon-only actions require content descriptions.
- Heatmaps/charts expose text summaries.
- 200% font and landscape must preserve primary actions.
- Auth continues to support paste, autofill, and password managers.

## Implementation boundaries

### Allowed by default

- XML layouts, styles, themes, colors, dimensions, fonts, vector drawables, selectors, transitions, and strings.
- View/adapters presentation, item decoration, UI-only state mapping, progressive disclosure, and bottom sheets.
- Custom visual Views that consume existing persisted data.
- Small Activity/Fragment/ViewModel changes needed to bind existing state to new presentation.

### Requires explicit justification and focused regression tests

- Repository signatures, domain calculations, service behavior, notification behavior, navigation destinations, or data queries.

### Out of scope

- Room entities/migrations, SQLCipher/Keystore behavior, Firebase Auth/Firestore/RTDB models or rules, account deletion stages, Guard decisions, reminder scheduling, export encryption, and business semantics.

If a desired visual cannot be implemented without changing an out-of-scope interface, stop and document the dependency before editing it.

## Rollout strategy

1. Add font/logo resources and semantic design tokens.
2. Build reusable component styles and states.
3. Redesign Auth/Profile first to validate the system.
4. Redesign Main shell and Today.
5. Redesign Habits and Guard.
6. Redesign Insights and Circle.
7. Redesign settings, detail/editor screens, focus, intervention, support, and privacy.
8. Perform copy pass across all active resources.
9. Verify screenshots, accessibility, rotation, BG7 performance, and behavior regression suites.

Each screen family must compile and pass its existing behavior tests before moving to the next.

## Acceptance criteria

- New logo is correctly integrated into launcher, splash, Auth, themed, and notification variants.
- Sign In, Create Account, verification, reset, and Profile look consistent and premium.
- Existing profile image selection/removal remains functional and encrypted locally.
- Today contains a real event-derived 12-week consistency heatmap and accessible summary.
- Guard, Insights, and Circle retain useful information upfront without equal-weight card stacks.
- No active screen uses generic motivational AI-style copy.
- No core feature, persistence path, cloud rule, notification, service, or security behavior regresses.
- All focused UI/unit/instrumentation suites, `assembleDebug`, and `lintDebug` pass.
- Complete BG7 walkthrough works at default and 200% font scale with animations disabled.
