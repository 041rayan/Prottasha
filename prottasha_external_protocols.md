---
document: prottasha_external_protocols
project: Prottasha (Android, digital/dopamine addiction, identity-over-willpower)
purpose: Protocols NOT in Atomic Habits or Psycho-Cybernetics, drawn from published research and clinical practice. Complements (does not replace) prottasha_book_protocols.md. Protocols only; no app-build instructions.
companion_file: prottasha_book_protocols.md   # IDs there: AH-xx, PC-xx, AX-x, C-x
this_file_ids: EX-xx
research_date: 2026-11-08
method: Web search of abstracts, registries, and review pages. Full papers were NOT read. Numbers below are from abstracts/summaries. Where a protocol rests on clinical convention I could not verify in this session, it is labelled so.
evidence_labels:
  RCT: randomized trial(s) directly on the behavior
  META: systematic review / meta-analysis
  FIELD: field study without a randomized control
  INDIRECT: evidence from a related condition (e.g. substance use) applied to behavioral addictions
  CLINICAL_CONVENTION: standard clinical practice, not re-verified in this session
  DESIGN_PRINCIPLE: engineering/safety guardrail with no outcome evidence behind it
how_to_read:
  - maps_to uses the same module codes as the companion file: IB, 4L, DAT, TM, HBB, SS, PR, PAUSE, BLOCK, SLEEP.
  - "complements" lists companion-file IDs this protocol extends or qualifies.
  - Section 4 lists where this file changes or corrects an assumption in the first file. Read it.
---

# 1. Friction, blocking, and tracking

```yaml
- id: EX-01
  name: Pause interstitial = delay + explicit exit (not just a prompt)
  evidence: FIELD + RCT(online component test)
  source: Grüning, Riedel, Lorenz-Spreen. PNAS 120(8), 2023 (self-nudge app "one sec"). https://pubmed.ncbi.nlm.nih.gov/36795756/
  finding:
    - 280 users, 6 weeks of field data: actual openings of chosen target apps fell 57% by week 6; one summary reports users backed out on about 36% of attempts and attempts were 37% lower than week 1.
    - Component test (preregistered online experiment, N=500): the option to dismiss had the strongest effect; the short waiting time also reduced consumption; the deliberation message alone was not effective.
    - Disclosure: the app's creator co-conducted the study per the developer's own site. The field part is within-user change, not a randomized control.
  protocol:
    - Intercept an attempt to open a user-chosen target app.
    - Insert a short wait (published versions use roughly 5-10 seconds).
    - Present a clear, low-effort way to leave without opening the app.
    - Optional, untested: ask the user to state intended use time (a registered trial uses this; no result seen).
  design_implication: In the Prottasha pause screen, "Is this your identity or your habit?" is the message component, which the best available evidence says is the weakest part. The delay and the easy exit carry the effect. Keep the identity prompt, but it should not be the only mechanism.
  complements: [AH-04, AH-12, AH-13, PC-13]
  maps_to: [PAUSE, BLOCK, TM]

- id: EX-02
  name: Grayscale as design friction
  evidence: RCT + FIELD
  sources:
    - Zimmermann & Sobolev. Cyberpsychol Behav Soc Netw 26(1), 2023 (N=112, pre-registered). https://pubmed.ncbi.nlm.nih.gov/36577008/
    - Holte et al. grayscale study (N=84, 1-week intervention). https://journals.sagepub.com/doi/10.1177/20501579231212062
    - Wickord & Quaiser-Pohl. Computers in Human Behavior Reports 10, 2023 (24h+ grayscale, N=92 at follow-up).
  finding:
    - Grayscale produced an immediate, significant drop in measured screen time versus a self-monitoring control. Time-limit goal-setting gave a smaller, more gradual drop.
    - Prior studies cited in the Holte paper: roughly 22 to 50 minutes/day less after a week or more. Effect on number of unlocks is inconsistent.
    - No immediate causal effect on subjective well-being or academic performance was found in the Zimmermann trial.
    - Studies are short (days to about 3 weeks). Long-term persistence is unknown.
  protocol:
    - Offer grayscale as a user-activated mode, whole-device or scheduled (e.g. high-risk hours).
    - Pair it with a stated purpose (focus, evening wind-down), not as a promise of better mood.
  caution: Hold back any claim that grayscale improves well-being. The evidence supports reduced use time only.
  complements: [AH-13, AH-07]
  maps_to: [BLOCK, SLEEP, HBB]

- id: EX-03
  name: Tracking alone does not change behavior
  evidence: RCT
  source: Zimmermann & Sobolev (above); a cited line in the Holte paper says self-monitoring screen time alone does not change smartphone behavior.
  protocol: Any tracker must be coupled to at least one active intervention (friction, limit, or commitment). A control group that only tracked did not reduce use.
  complements: [AH-17, AH-19]
  maps_to: [DAT, PJ]

- id: EX-04
  name: Time limits are weaker than friction, use them as a second layer
  evidence: RCT (same trial as EX-02)
  protocol: Self-commitment to time limits produced a smaller and more gradual reduction than grayscale. Use limits to add accountability to a friction layer, not as the primary tool.
  maps_to: [BLOCK, DAT]
```

# 2. Behavior-specific protocols (gaps in the books)

## 2.1 Online gambling

```yaml
- id: EX-05
  name: Layered access barrier stack (self-exclusion + device blocking + money blocks + human support)
  evidence: INDIRECT/qualitative (stakeholder-involved evaluations)
  sources:
    - GamCare/Ipsos longitudinal study of multi-operator self-exclusion (2023) - qualitative, 30 follow-up interviews.
    - GambleAware-commissioned evaluation of blocking software (2018) - blocking judged best as part of a treatment package, not in isolation; large quality variation between products.
    - GamCare "TalkBanStop" evaluation (published 18 June 2026) - layered approach reported as stronger than any single tool. Evaluators and partners have commercial/organizational interests.
  layers:
    - L1 Block gambling sites/apps on the device.
    - L2 Formal self-exclusion with operators, where a scheme exists in the user's country.
    - L3 Money barriers: ask the bank/payment provider to block gambling transactions; reduce easy access to funds.
    - L4 Human support (counselor, helpline, accountability partner).
  limits_reported:
    - Offshore gambling and cryptocurrency routes were reported as gaps.
    - Users could circumvent tools in some cases.
    - Vendor-reported coverage figures (e.g. blocking about 99% of UK-licensed active sites) are UK-specific and vendor/commissioner-sourced. Coverage for Bangladesh-accessible operators was NOT verified.
  protocol: Treat blocking as time-buying ("speed bump"), not treatment. Present all four layers as a checklist the user completes over time.
  complements: [AH-13, AH-15, AH-20]
  maps_to: [BLOCK, SS, PR, HBB]

- id: EX-06
  name: Gambling screen - PGSI
  evidence: standard population screen
  source: Ferris & Wynne (2001) Canadian Problem Gambling Index; bands as published by UK Gambling Commission and others.
  format: 9 items, each 0-3 (never / sometimes / most of the time / almost always), past 12 months, total 0-27.
  bands: {0: non-problem, 1-2: low risk, 3-7: moderate risk, 8+: problem gambling with possible loss of control}
  caveats:
    - Screening, not diagnosis.
    - The 8+ cutoff is debated; one study proposed 10 for research use to limit false positives.
    - A shorter "mini-screen" version exists (Gambling Commission).
    - Check usage terms before embedding the items.
  use: Score on entry and periodically. Score 8+ triggers the referral protocol (EX-12).
  maps_to: [DAT, PR]
```

## 2.2 Pornography / compulsive sexual behavior (PMO)

```yaml
- id: EX-07
  name: Clinical-grade components for problematic pornography use
  evidence: META (high risk of bias reported)
  sources:
    - López-Pinar, Esparza-Reig, Bőthe. J Behav Addict 14(2), 2025 (20 studies, 2,021 participants). https://pubmed.ncbi.nlm.nih.gov/40126561/
    - Antons et al. J Behav Addict 11(3), 2022 (24 studies, 4 RCTs).
    - Zwielewski et al. Sexual Medicine Reviews 14(2), 2026 (scoping review of 11 CBT-based protocols).
  finding:
    - Psychotherapy (mostly CBT and ACT) beat controls with large effects on problematic use, frequency/duration, and sexual compulsivity; small effect on craving; moderate effect on depressive symptoms.
    - Risk of bias was high and protocols are not standardized.
    - Reviewed protocols report reduced use, craving, guilt/shame, and anxiety. No study in the 2026 scoping review reported complete abstinence.
    - Self-guided/online formats had high dropout and adherence problems.
  components_that_recur:
    - Cognitive restructuring
    - Stimulus control
    - Relapse-prevention skills
    - Acceptance/mindfulness strategies (reported to help with shame and emotional regulation)
  protocol:
    - Track frequency, duration, and craving as separate fields (they respond differently).
    - Let the user define the goal (reduction vs stopping). The research does not show that abstinence is the usual outcome.
    - Include a shame-reduction thread: reviews link distress to moral incongruence and sexual shame, so avoid moralizing copy.
    - Offer a therapist path, because the evidence is for therapist-delivered CBT/ACT and self-guided formats lose people.
  classification_note: ICD-11 lists compulsive sexual behavior disorder as an impulse-control disorder; a core criterion is repeated failure to stop despite substantial negative consequences.
  complements: [AH-08, AH-11, AH-18, PC-18]
  maps_to: [TM, DAT, HBB, PR, PJ]
```

## 2.3 Gaming and social-media / smartphone screening

```yaml
- id: EX-08
  name: Brief self-report screens for non-gambling behaviors
  evidence: psychometric validation studies
  instruments:
    - BSMAS (Bergen Social Media Addiction Scale): 6 items, 1-5 scale, total 6-30. Cutoffs in the literature disagree (>=19 and >=24 both appear).
    - IGDS9-SF (Internet Gaming Disorder Scale, short form): 9 items mapped to the nine DSM-5 criteria, 5-point scale.
    - SABAS (Smartphone Application-Based Addiction Scale): 6 items.
    - SAS-SV (Smartphone Addiction Scale, short version): 10 items; thresholds vary by study and population.
  rules:
    - Use as screening and trend lines, not diagnosis.
    - Because cutoffs disagree, prefer change over time plus a flag for high scores, not a hard label.
    - Verify each instrument's licensing/permission before embedding items.
    - Bangladeshi-Bangla validation of these scales was NOT checked.
  gap: For PMO, candidate screens (e.g. Brief Pornography Screen, Problematic Pornography Use Scale) were NOT verified in this session. Confirm before use.
  complements: [AH-03, AH-19]
  maps_to: [DAT, PR]
```

# 3. Urge handling, relapse, motivation, referral, safety, data

```yaml
- id: EX-09
  name: Urge surfing and brief mindful pause
  evidence: META (INDIRECT - substance use, small effects)
  source: Grant et al. J Addict Med 11(5), 2017 (9 RCTs, 901 participants); RAND RR1031.
  finding:
    - No significant difference from comparators on relapse (OR 0.72, 95% CI 0.46-1.13) or frequency of use.
    - Significant small benefits on craving/withdrawal symptoms (SMD -0.13) and negative consequences of use (SMD -0.23). Evidence quality rated low.
    - Evidence is from substance use, not gambling/gaming/PMO. Transfer is assumed.
  protocol:
    - Name the urge, notice where it sits in the body and how it changes, and let it rise and pass without acting.
    - Short "breathing space" version for in-the-moment use (the MBRP program includes a brief breathing-space exercise; the full acronym was not retrieved).
  positioning: Optional coping skill. Do not market it as proven relapse prevention.
  complements: [PC-13, AH-12]
  maps_to: [TM, PAUSE]

- id: EX-10
  name: Relapse-prevention model (high-risk situations, lapse vs relapse)
  evidence: CLINICAL_CONVENTION (the high-risk-situation approach is described as the most common relapse-prevention intervention in the MBRP review; the abstinence-violation items below are standard Marlatt & Gordon 1985 framework, not re-verified here)
  protocol:
    - Build a personal inventory of high-risk situations (time, place, emotion, people, device state).
    - Pair each situation with a specific coping response (links to if-then plans).
    - Treat a lapse as a single event to analyze, not proof of failure. A lapse followed by shame and "I blew it" thinking is the known path to full relapse.
    - After a lapse, run a short chain review: what preceded it, what the urge felt like, which coping step failed or was skipped, what changes next time.
  complements: [AH-18, PC-18, PC-21, AH-06]
  maps_to: [TM, PJ, HBB, DAT]

- id: EX-11
  name: Ambivalence handling (decisional balance)
  evidence: CLINICAL_CONVENTION (Motivational Interviewing; not re-verified here)
  protocol:
    - Let the user list what the behavior gives them and costs them, and what changing gives and costs.
    - Reflect the user's own stated reasons for change back to them; do not argue for change.
    - Revisit when motivation drops, because ambivalence is expected, not a failure.
  complements: [AH-01, PC-05]
  maps_to: [IB, TM, PJ]

- id: EX-12
  name: Referral triggers (when the app should point to professionals)
  evidence: screening-tool guidance + ICD-11 criterion + CLINICAL_CONVENTION
  triggers:
    - PGSI 8+ (published guidance says to seek assistance at this level).
    - Repeated failed attempts to stop, with continued use despite clear harm (core of the ICD-11 compulsive sexual behavior disorder definition; same logic applies to gambling and gaming).
    - Financial harm: debt, borrowing or selling things to gamble, chasing losses (PGSI items; UK survey data link higher PGSI to problem debt).
    - Loss of study/work/relationship opportunities because of the behavior.
    - Co-occurring depression, anxiety, or sleep collapse. (Rationale is clinical convention, not verified in this search.)
    - Any suicidal thoughts: go to EX-13 first, not to a directory.
  protocol: Show referral as a normal next step on a ladder (peer/pod -> counselor/therapist -> psychiatrist), not a failure state. Do not wait for a lapse.
  complements: [PC-08, AH-13 caveat]
  maps_to: [PR]

- id: EX-13
  name: Crisis safety protocol
  evidence: CLINICAL_CONVENTION + sourced local numbers (verify at runtime)
  trigger: User expresses suicidal thoughts, self-harm intent, or hopelessness with a plan, in journaling, chat, pod posts, or check-ins.
  protocol:
    - Stop normal flow and show crisis resources first. Keep the text short, calm, and non-judgmental.
    - Encourage contacting a trusted person right now and local emergency services if there is immediate danger.
    - Do not offer self-harm substitutes (pain, shock) as coping tools.
    - Do not make absolute promises about confidentiality or what responders will do.
  bangladesh_numbers:   # directory data must be remotely updatable; hours change
    emergency: "999"
    kaan_pete_roi: {number: "09612-119911", hours: "3pm-3am daily per LifeLine International; older sources say 24/7, so verify", type: emotional support and suicide prevention, volunteer-staffed}
    vent_by_mindspace: {number: "09678-678-778", hours: "6pm-6am per one aggregator (TherapyRoute); single secondary source, verify"}
    national_helpline_109: {note: "described as legal guidance and psychosocial counselling for women and children; hours not published by government pages found"}
  gap_note: One aggregator states Bangladesh has no emotional-support line open every hour. Always show 999 alongside the helplines and a trusted-contact option.
  maps_to: [PR, PJ, SS, PAUSE]

- id: EX-14
  name: Sleep protocol (CBT-I components that actually matter)
  evidence: META
  sources:
    - Components and delivery formats of CBT for chronic insomnia, component NMA. JAMA Psychiatry 81(4), 2024. https://pubmed.ncbi.nlm.nih.gov/38231522/
    - Fully automated digital CBT-I meta-analysis (29 RCTs, 9,475 participants). https://pmc.ncbi.nlm.nih.gov/articles/PMC11903857
    - CBT-I setting network meta-analysis, Sci Rep 2023 (52 RCTs). https://link.springer.com/10.1038/s41598-023-28853-0
    - Active components of digital sleep interventions in adolescents, npj Digital Medicine, Dec 2025.
  finding:
    - Critical components: cognitive restructuring, third-wave (acceptance/mindfulness) components, sleep restriction, stimulus control.
    - Sleep hygiene education was not essential. Relaxation procedures were possibly counterproductive (iOR 0.81, 95% CI 0.64-1.02; interval includes 1, so not conclusive).
    - Fully automated digital CBT-I beat most controls but was less effective than therapist-assisted CBT-I. In one network analysis, smartphone-delivered CBT-I did not reach significance on insomnia severity versus waitlist.
  protocol_safe_for_app:
    - Fixed wake time (CBT-I convention).
    - Stimulus control: use bed for sleep; if awake for a long stretch, leave bed and return when sleepy (the ~20-minute rule is standard CBT-I practice, source here is a secondary summary).
    - Keep phone out of reach of the bed (friction, see EX-01/EX-02; also AH-25).
    - Evening grayscale/blocking window as a bedtime boundary (EX-02).
    - Short sleep diary as a tracker.
    - Cognitive restructuring of sleep worries (pairs with PC-05).
  protocol_not_for_unsupervised_app:
    - Sleep restriction is a potent component but is a clinician-supervised procedure in standard practice. Known cautions (e.g. daytime sleepiness and drowsy driving, bipolar disorder, seizure disorders) come from clinical convention and were not verified here. Route to a professional instead of auto-prescribing it.
  complements: [AH-25, AH-07, AH-15]
  maps_to: [SLEEP, BLOCK, PR]

- id: EX-15
  name: Pod and anonymous-support guardrails
  evidence: DESIGN_PRINCIPLE (no outcome evidence reviewed)
  protocol:
    - Peer messages are support, not treatment or medical advice.
    - Crisis language in any post triggers EX-13.
    - Ban explicit content and method details (gambling tactics, circumvention of blockers) in posts.
    - Reporting, blocking, and pod-leave must be possible at any time.
    - Pseudonymous identities by default; do not imply anonymity is perfect if the system can link accounts.
    - A partner who holds a lock or sees a contract (AH-15, AH-20) needs explicit user consent and a clear list of what they can see.
  maps_to: [SS, PR]

- id: EX-16
  name: Sensitive-data handling for stigmatized behaviors
  evidence: DESIGN_PRINCIPLE
  protocol:
    - Treat gambling, PMO, and mental-health entries as high-sensitivity data.
    - Neutral notification and widget text; no behavior names on lock screens.
    - Local-first storage with app lock; sync only what the user opts to sync.
    - User-controlled export and full deletion.
    - Do not infer or label a diagnosis from logs or scores; show raw scores and trends.
  maps_to: [DAT, PJ, SS, PR]

- id: EX-17
  name: Engagement and dropout management
  evidence: META (dropout is repeatedly reported for self-guided formats)
  sources: PPU treatment reviews (Roza et al. 2023 and 2026 scoping review above); dCBT-I meta-analyses (therapist-assisted > fully automated).
  protocol:
    - Expect high attrition in self-guided use and design for return: low-effort check-ins, reminders tied to the user's own if-then plans, and a path back after a break (links to AH-18).
    - Add human touchpoints (partner, pod, optional clinician) because guided formats outperform fully automated ones in sleep trials.
  maps_to: [HBB, PJ, SS]
```

# 4. How this file changes or corrects the first file

```yaml
reconciliations:
  - id: R-1
    topic: Pause screen
    first_file: PAUSE mapped to AH-01 decision question + AH-04 + PC-13.
    update: EX-01 says the message is the weak component. Keep the identity prompt, but build the pause as delay + easy exit + optional urge-surf/breath step.
  - id: R-2
    topic: Sleep mode
    first_file: SLEEP mapped to PC-04 (relaxation imagery) and PC-14 (quiet room).
    update: The 2024 JAMA Psychiatry component analysis found relaxation procedures possibly counterproductive for insomnia (not conclusive). Treat PC-04/PC-14 as optional wind-down for stress, not as the sleep protocol. Lead with fixed wake time, stimulus control, and device friction (EX-14, AH-25).
  - id: R-3
    topic: Grayscale
    first_file: marked "none" (not covered).
    update: Now covered by EX-02. Evidence supports reduced use time, not better well-being.
  - id: R-4
    topic: Tracking and streaks
    first_file: AH-17 recommends trackers and "don't break the chain".
    update: EX-03 says tracking alone did not reduce use. Streaks must sit on top of an active intervention. AH-18 and AH-19 still apply.
  - id: R-5
    topic: Referral module
    first_file: "needs its own clinical sourcing".
    update: EX-06, EX-08, EX-12, EX-13 supply screening, triggers, and crisis handling. Directory contents for Bangladesh (licensed therapists, clinics) were NOT found or verified.
  - id: R-6
    topic: Blocking claims
    first_file: AH-13/AH-15 say friction tricks are unlikely to stop a true addiction.
    update: EX-05 agrees: blocking works as a layer, and for gambling needs money barriers and human support.
```

# 5. Module map for this file

| Component | New protocols |
|---|---|
| Identity Blueprint | EX-11 |
| Four Laws Engine | none directly (EX-01/EX-02 extend the friction laws) |
| Digital Addiction Tracker | EX-03, EX-04, EX-06, EX-07, EX-08, EX-10, EX-16 |
| Trigger Management | EX-01, EX-07, EX-09, EX-10, EX-11 |
| Habit Builder/Breaker | EX-02, EX-05, EX-07, EX-10, EX-17 |
| Social Support | EX-05, EX-13, EX-15, EX-16, EX-17 |
| Progress Journaling | EX-03, EX-07, EX-10, EX-11, EX-13, EX-16, EX-17 |
| Professional Referral | EX-05, EX-06, EX-08, EX-12, EX-13, EX-14, EX-15, EX-16 |
| Pause screen | EX-01, EX-09, EX-13 |
| Site/app blocking | EX-01, EX-02, EX-04, EX-05, EX-14 |
| Sleep mode | EX-02, EX-14 |
| Grayscale focus | EX-02 |

# 6. Open verification list (do these before shipping)

```yaml
unverified_or_conflicting:
  - Helpline hours: sources disagree (24/7 vs 3pm-3am for Kaan Pete Roi). Confirm directly with each service and keep the directory remotely editable.
  - PMO-specific screening scales: not checked.
  - Bangla validation and licensing for BSMAS, IGDS9-SF, SABAS, SAS-SV, PGSI: not checked.
  - Gambling blocklist coverage for operators reachable from Bangladesh: not checked. Cited coverage numbers are UK, vendor-involved.
  - Bangladesh legal status of online gambling: not researched.
  - Sleep-restriction safety cautions and the stimulus-control timing are from clinical convention, not from sources read here.
  - EX-10 (Marlatt framework) and EX-11 (Motivational Interviewing) are standard clinical frameworks cited from established practice; no source was opened in this session for their details.
  - All figures come from abstracts and summaries. Read the primary papers before quoting numbers inside the app.
```
