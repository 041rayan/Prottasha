---
document: prottasha_book_protocols
project: Prottasha (Android, digital/dopamine addiction, identity-over-willpower)
purpose: Protocol library extracted from two books for an AI agent to map onto app modules. Protocols only. No app-build instructions.
sources:
  AH: "Atomic Habits" (James Clear) - ch. numbers refer to the book
  PC: "Psycho-Cybernetics, Updated and Expanded" (Maxwell Maltz, updated with Matt Furey notes) - ch. numbers refer to the book
extraction_method: Full-text skim of both epubs; every protocol below is paraphrased from the text. Fill-in templates (short formulas) are kept verbatim because the formula is the protocol.
how_to_read:
  - Each protocol has a stable ID (AH-xx / PC-xx). Reference IDs, do not restate content.
  - "template" = fill-in sentence the user completes. "steps" = ordered procedure. "params" = numbers the book gives. "data" = fields a protocol naturally produces or consumes.
  - "maps_to" lists Prottasha modules: IB=Identity Blueprint, 4L=Four Laws Engine, DAT=Digital Addiction Tracker, TM=Trigger Management, HBB=Habit Builder/Breaker, SS=Social Support, PJ=Progress Journaling, PR=Professional Referral, PAUSE=pause screen, BLOCK=site/app blocking, SLEEP=sleep mode.
  - Section 5 lists what the books do NOT cover and where the two books conflict. Read it before relying on any protocol.
---

# 1. Shared theory (one-line axioms the protocols rest on)

```yaml
axioms:
  - id: AX-1
    src: AH ch.2 / PC ch.1
    text: Behavior follows self-concept. AH mechanism = identity is built from accumulated evidence (each action is a "vote"). PC mechanism = the self-image is a goal-image the brain steers toward, built from vivid imagery plus remembered experience.
  - id: AX-2
    src: AH ch.7
    text: People with good self-control mostly avoid tempting situations rather than resisting them. Willpower is a short-term strategy; environment is the long-term one.
  - id: AX-3
    src: AH ch.3, ch.10
    text: Every habit = cue -> craving -> response -> reward. The craving comes from a prediction about how the response will change your internal state, and it serves an underlying motive.
  - id: AX-4
    src: AH ch.15
    text: What is immediately rewarded is repeated; what is immediately punished is avoided. Delayed rewards lose to instant ones.
  - id: AX-5
    src: PC ch.2, ch.14
    text: The brain's goal-seeking system cannot tell vividly imagined experience from real experience, and it corrects course by trial and error (negative feedback). Mistakes are data, not verdicts.
  - id: AX-6
    src: AH ch.7 (design constraint)
    text: Shaming and fear-based messaging can raise stress and push people back to their coping habit. Tone constraint for any pause/relapse messaging.
```

# 2. Atomic Habits protocols

## 2.1 Identity

```yaml
- id: AH-01
  name: Identity-first two-step
  src: AH ch.2
  steps:
    - Decide the type of person you want to be (values, what you stand for).
    - Prove it to yourself with small wins.
  variant_working_backward: Start from the outcome you want, ask "Who is the type of person who could get this outcome?", then switch focus from the outcome to being that type of person.
  rules:
    - Every action is a vote for an identity. Bad-habit actions are votes too.
    - Need a majority of votes, not a unanimous one. One slip does not decide the election.
    - Three levels of change: outcome, process, identity. Target identity.
  decision_question: "What would a [desired identity] person do?" (used as an in-the-moment guide)
  data: [identity_statement, supporting_actions, vote_count_for_vs_against]
  maps_to: [IB, PAUSE, PJ]

- id: AH-23
  name: Keep identity flexible
  src: AH ch.20
  rules:
    - Do not let one role or label become the whole identity (keep identity small).
    - Restate role-based identities as type-of-person identities so they survive changes (example pattern: "I'm an athlete" -> "I'm someone who is mentally tough and likes physical challenge").
  maps_to: [IB]
```

## 2.2 Awareness

```yaml
- id: AH-02
  name: Habit loop diagnosis
  src: AH ch.3
  fields: [cue, craving, response, reward]
  phases: {problem_phase: [cue, craving], solution_phase: [response, reward]}
  rule: All behavior is an attempt to solve a problem. Log the problem the habit is solving.
  example_in_domain: Stuck on a task (cue) -> want relief from frustration (craving) -> open social media (response) -> relief (reward), which links the habit to feeling stalled.
  maps_to: [DAT, TM, HBB]

- id: AH-03
  name: Habits Scorecard
  src: AH ch.4
  steps:
    - List the day's behaviors in order, as a plain sequence.
    - Mark each + (good), - (bad), = (neutral).
    - Judge by one question: does this behavior cast a vote for or against my desired identity?
    - Observe without judging or praising. Awareness only; no change required at first.
  note: Book says "good/bad" really means effective at solving some problem. Judge relative to the desired identity.
  data: [ordered_behavior_list, rating(+,-,=)]
  maps_to: [DAT, IB, 4L]

- id: AH-04
  name: Pointing-and-Calling
  src: AH ch.4
  steps: Say out loud the action you are about to take and its likely outcome.
  rationale: Speaking it raises a nonconscious habit to conscious awareness and adds weight to the moment. Book cites large error reductions from the rail-industry version.
  maps_to: [PAUSE, TM]
```

## 2.3 The Four Laws (rule catalogue for the Four Laws Engine)

Book's own numbering. Create-good-habit and break-bad-habit are mirror images.

| Law | Build a good habit | Break a bad habit (inversion) | IDs |
|---|---|---|---|
| 1 Obvious | 1.1 scorecard; 1.2 implementation intention; 1.3 habit stacking; 1.4 design environment so cues are visible | 1.5 reduce exposure: remove cues of the bad habit | AH-03, AH-05, AH-06, AH-07, AH-08 |
| 2 Attractive | 2.1 temptation bundling; 2.2 join a culture where the behavior is normal; 2.3 motivation ritual | 2.4 reframe: highlight benefits of avoiding the habit | AH-09, AH-10, AH-12 |
| 3 Easy | 3.1 reduce friction; 3.2 prime environment; 3.3 master the decisive moment; 3.4 two-minute rule; 3.5 automate | 3.6 increase friction; 3.7 commitment device | AH-13, AH-14, AH-15 |
| 4 Satisfying | 4.1 immediate reinforcement; 4.2 make "doing nothing" enjoyable; 4.3 habit tracker; 4.4 never miss twice | 4.5 accountability partner; 4.6 habit contract | AH-16, AH-17, AH-18, AH-20 |

## 2.4 Law 1: Make it obvious / invisible

```yaml
- id: AH-05
  name: Implementation intention
  src: AH ch.5
  template: "I will [BEHAVIOR] at [TIME] in [LOCATION]."
  generic_form: "When situation X arises, I will perform response Y."
  params: Book cites a study where a when-and-where plan took weekly exercise from about 35-38% to 91%; extra motivational material did not help.
  rules:
    - Lack of clarity is often mistaken for lack of motivation.
    - Pick a fresh-start date (first day of week/month/year) when unsure when to begin.
    - Specificity also makes it easier to refuse competing requests.
  maps_to: [HBB, TM, 4L]

- id: AH-06
  name: Habit stacking (and if-then plans)
  src: AH ch.5
  template: "After [CURRENT HABIT], I will [NEW HABIT]."
  situational_form: "When [situation], I will [response]."   # book gives many, e.g. wait 24h before a purchase over a set amount
  rules:
    - Anchor to a habit that already happens reliably and at the same frequency as the new one.
    - Chains are allowed: each habit cues the next.
    - New behaviors can be inserted mid-routine.
    - Brainstorm anchors by listing current daily habits (use the scorecard).
  maps_to: [HBB, TM, 4L]

- id: AH-07
  name: Environment design for cues
  src: AH ch.6
  rules:
    - Make the cue for a good habit visible in the place where it will be used. Several cues per habit increase the odds.
    - Context becomes the cue: the whole setting triggers the habit, not one object.
    - "One space, one use": give each activity its own location. When space is limited, divide it into zones.
    - Applies to digital spaces: assign devices/accounts to single purposes (example: one device only for writing, one only for reading, phone only for social media and texting).
    - Mixed-use contexts let the easier (often worse) habit win; the phone is the standard offender.
    - New habits are easier to start in a new context because old cues are absent.
  maps_to: [HBB, BLOCK, SLEEP]

- id: AH-08
  name: Make it invisible (cue removal)
  src: AH ch.7
  rules:
    - Cutting exposure to the cue is the most practical way to remove a bad habit.
    - Examples given: phone in another room for hours; unfollow accounts that trigger envy; remove the console/TV after use.
    - Habits are not forgotten once encoded; the urge returns when cues return ("cue-induced wanting"). Cue exposure is the lever.
  evidence_cited: Study of soldiers addicted to heroin in Vietnam: most did not relapse after returning home because the cue environment changed. Used by the book to argue addiction is strongly context-dependent, not simply moral weakness.
  maps_to: [BLOCK, TM, HBB, DAT]
```

## 2.5 Law 2: Make it attractive / unattractive

```yaml
- id: AH-09
  name: Temptation bundling
  src: AH ch.8
  principle: Pair something you want with something you need (Premack's principle).
  template:
    - "After [CURRENT HABIT], I will [HABIT I NEED]."
    - "After [HABIT I NEED], I will [HABIT I WANT]."
  example_in_domain: "After I pull out my phone, I will do ten burpees. After ten burpees, I will check [app]."
  maps_to: [HBB, 4L]

- id: AH-10
  name: Join a culture where the behavior is normal
  src: AH ch.9
  model: People copy three groups - the close (family/friends), the many (the tribe), the powerful (high status).
  rules:
    - Best move: join a group where (1) the desired behavior is normal and (2) you already share something with the group.
    - Belonging is a stronger pull than the individual's intention; the group norm usually wins.
  maps_to: [SS]

- id: AH-11
  name: Craving analysis (surface craving vs underlying motive)
  src: AH ch.10
  steps:
    - Name the surface craving (e.g. "check the feed").
    - Name the underlying motive it serves. Book's motive list: conserve energy; food/water; love; bonding; social approval; reduce uncertainty; status.
    - Find other ways to satisfy the same motive.
  rule: Habits are modern solutions to ancient desires; the current habit is not necessarily the best solution.
  data: [surface_craving, underlying_motive, alternative_responses]
  maps_to: [TM, HBB, DAT]

- id: AH-12
  name: Reframing and motivation ritual
  src: AH ch.10
  techniques:
    - Swap "I have to" for "I get to" (both are true; choose the frame that shows the benefit).
    - For bad habits, deliberately state the benefits of not doing it.
    - Motivation ritual: do something enjoyable immediately before a hard habit so the ritual itself becomes the cue for the right state. Book's example: three slow breaths plus a smile before a pleasant act, later reused as a mood-reset.
  maps_to: [PAUSE, TM, HBB]
```

## 2.6 Law 3: Make it easy / difficult

```yaml
- id: AH-13
  name: Friction control and environment priming
  src: AH ch.12
  rules:
    - People follow the path of least effort. Lower the steps to good behavior; raise the steps to bad behavior.
    - Prime the environment the night before for the next good action.
    - Bad-habit friction examples: unplug the device and only plug in if you can say what you will use it for; remove batteries; store it out of the room; phone in another room until a set time; ask another person to hold the device for a few hours; delete apps (the book says re-installing often takes weeks).
    - Caveat in the text: these tricks are unlikely to stop a true addiction. See section 5.
    - Optimize "decisive moments" (small choices that steer the next hours).
  maps_to: [BLOCK, HBB, TM]

- id: AH-14
  name: Two-Minute Rule and habit shaping
  src: AH ch.13
  rule: A new habit must be scalable down to a version that takes two minutes or less (e.g. "read one page", "open my notes", "put on running shoes").
  purpose: Master showing up first; standardize before optimizing.
  variant: Do only the two-minute version and stop.
  habit_shaping: Break a large change into 4-5 phases, each only slightly bigger than the last (book example for becoming an early riser: be home by a set time -> devices off -> in bed -> lights off -> wake time).
  maps_to: [HBB, 4L]

- id: AH-15
  name: Commitment devices and automation
  src: AH ch.14
  definition: A choice made now that locks in future behavior.
  examples_given:
    - Website blocker to cut social media browsing.
    - Turn off notifications, silence the phone, mute group chats, delete games and social apps.
    - Another person resets your social-media passwords weekly so access only returns at the end of the work week.
    - TV out of the bedroom; blackout curtains; good mattress (sleep).
  rules:
    - One-time setup actions beat repeated willpower.
    - Convenience technology works against you too: friction-free autoplay and endless feeds make stopping harder than continuing.
  maps_to: [BLOCK, SLEEP, SS]
```

## 2.7 Law 4: Make it satisfying / unsatisfying

```yaml
- id: AH-16
  name: Immediate reinforcement and visible avoidance
  src: AH ch.15
  rules:
    - The end of a behavior is what gets remembered; make it feel like a success.
    - Make avoidance visible: for each avoided temptation, move a fixed amount to a labeled goal account (book's pattern: label the account for something wanted).
    - Reward must match the identity (do not reward with the thing the identity opposes).
    - Over time the identity itself becomes the reward.
  maps_to: [HBB, PJ, PAUSE]

- id: AH-17
  name: Habit tracker
  src: AH ch.16
  benefits: [visual cue, motivating progress, satisfying to record]
  rules:
    - Automate measurement where possible.
    - Manually track only the most important habits (one tracked consistently beats ten tracked sporadically).
    - Record right after the habit (stack: "After [HABIT], I will [TRACK IT]").
    - Never-break-the-chain streak as motivator.
  maps_to: [DAT, PJ, HBB]

- id: AH-18
  name: Never miss twice (relapse protocol)
  src: AH ch.16
  rules:
    - One miss is an accident; two in a row starts a new habit.
    - Avoid all-or-nothing thinking. On bad days do a reduced version rather than zero.
    - Rebounding fast matters more than the slip. A cited study (endnote) found one missed day had little effect on long-term habit formation if the person resumed.
  data: [days_since_last_success, consecutive_misses]
  maps_to: [PJ, HBB, DAT, TM]

- id: AH-19
  name: Measurement pitfalls
  src: AH ch.16
  rules:
    - When a measure becomes a target it stops being a good measure (Goodhart). Do not let a streak or a screen-time number become the purpose.
    - Add non-numeric/non-scale wins (sleep, mood, energy) alongside the metric.
  maps_to: [DAT, PJ]

- id: AH-20
  name: Accountability partner and habit contract
  src: AH ch.17
  contract_fields: [commitment_statement, phases_or_roadmap, daily_habits, consequence_if_missed, signatories(self + 1-2 observers)]
  rules:
    - Make the cost of the bad habit public and immediate. Another person watching creates an immediate cost to inaction.
    - Consequences can escalate as goals are upgraded.
    - Signing signals seriousness.
    - Can be automated (a pre-scheduled penalty that fires on failure).
  maps_to: [SS, 4L, HBB]
```

## 2.8 Progression, timing, review

```yaml
- id: AH-21
  name: Goldilocks rule
  src: AH ch.19
  rule: Motivation peaks when difficulty is just manageable. Book cites roughly 4% beyond current ability. Once a habit is established, raise difficulty slightly and keep some variety to prevent boredom.
  maps_to: [HBB, 4L]

- id: AH-22
  name: Reflection and review
  src: AH ch.20
  cadence_in_book: annual review (December) and mid-year integrity report. Short; a few hours a year.
  annual_review_questions: [What went well?, What didn't go so well?, What did I learn?]
  integrity_report_questions: [What core values drive my life and work?, How am I living and working with integrity right now?, How can I set a higher standard in the future?]
  other_tools:
    - Baseline-then-improve-by-1% scoring against your own past numbers.
    - Decision journal: record decision, reason, expected outcome; review later.
  note: Weekly/daily cadence is Prottasha's design, not the book's. The questions transfer; the cadence is yours.
  maps_to: [PJ, IB]

- id: AH-24
  name: Habit formation is about repetitions, not days
  src: AH ch.11
  rules:
    - Automaticity comes from the number of repetitions, not elapsed time. The book explicitly rejects a fixed 21- or 30-day rule.
    - Practice beats planning: do the reps.
  maps_to: [HBB, PJ]  # see conflict C-1

- id: AH-25
  name: Bed = sleep only
  src: AH ch.6
  rules:
    - Study cited: insomniacs told to go to bed only when sleepy, and to leave the bed/room if not asleep; bed became tied to sleeping.
    - Do not use the sleep context for phone, TV, or clock-watching. Move the TV out of the bedroom.
  maps_to: [SLEEP]
```

# 3. Psycho-Cybernetics protocols

## 3.1 Model and rules

```yaml
- id: PC-01
  name: Self-image and goal-image model
  src: PC ch.1-2
  claims_in_book:
    - The self-image sets the "area of the possible" for what you attempt and achieve.
    - A goal-striving mechanism (the book calls it the Creative/Success Mechanism) works on the mental image you give it. Feed it success goals or failure goals and it works impersonally on either.
    - Building a new self-image needs a reason to believe the old picture is wrong. Pure willpower declarations do not stick.
  maps_to: [IB]

- id: PC-02
  name: Five operating rules of the success mechanism
  src: PC ch.2
  rules:
    - R1: Give it a clear goal, framed as already existing in potential now.
    - R2: Think in end results; do not demand the "how" before the goal is clear.
    - R3: Do not fear mistakes. Progress is going forward, erring, correcting.
    - R4: Learn by remembering successful responses and dropping past errors.
    - R5: Trust and allow; do not jam it with anxious over-control.
  maps_to: [IB, PJ, TM]
```

## 3.2 Daily practices (the rehearsal protocols)

```yaml
- id: PC-03
  name: Daily identity visualization (mental picturing)
  src: PC ch.3
  params: Book's full version = 30 min/day, alone, undisturbed. Updated edition says 5-10 min can already help and 10-15 min can be powerful. Daily frequency is the key variable.
  steps:
    - Relax first: scan the body for tension, relax muscle groups, slow deep breathing, a light smile.
    - Recall a real past success (any size) to evoke a positive feeling now.
    - Carry that feeling into a picture of the ideal self acting, feeling, and being as desired. Make it vivid: sights, sounds, objects, small details. A "movie of myself on a screen" works for some.
    - Frame it as "I'm imagining this now for this session", not as a promise about tomorrow.
    - If the mind wanders or the image is weak, continue; clarity improves with practice. Do not self-criticize.
  data: [session_date, duration_min, scenario_text, felt_intensity]
  maps_to: [IB, PJ]

- id: PC-04
  name: Relaxation imagery set
  src: PC ch.4
  params: 10-30 min/day. Seated or standing is preferred over lying down because sleeping reduces the benefit.
  pictures:
    - P1: limbs as heavy concrete sinking into the bed.
    - P2: body as a limp marionette with slack strings.
    - P3: body as inflated balloons slowly deflating from the feet up.
    - P4: replay a detailed memory of a calm, pleasant scene (include sounds and small details).
  carry_over: During the day, pause briefly and recall the relaxed sensation in detail to re-induce it.
  maps_to: [TM, PAUSE, SLEEP]

- id: PC-14
  name: Quiet room (mental decompression)
  src: PC ch.12
  steps:
    - Build, in imagination, a detailed private room (furnish it with whatever is restful).
    - Enter it on cue: narrate climbing the stairs, opening the door, sitting down, noticing details.
    - Use for a few minutes between tasks or when tension rises. Also use it to clear emotional carry-over before starting a new task.
  maps_to: [TM, PAUSE, SLEEP]

- id: PC-16
  name: Practice without pressure (mental rehearsal / dry run)
  src: PC ch.13
  rationale: Over-motivation and pressure impair performance. Rehearse the correct response in a no-stakes setting so it is available under pressure (fire-drill and shadowboxing analogies).
  steps:
    - Rehearse the challenging scene in imagination and out loud, in private, repeatedly.
    - Include handling of unexpected turns, not just the ideal script.
    - Variant for anxious performance: first only observe ("bat on shoulder"), then add the intention, then act.
  in_domain: Rehearse the exact temptation moment and the planned response (pairs with AH-05/AH-06 if-then plans).
  maps_to: [TM, IB, HBB]
```

## 3.3 Belief, memory, and feeling protocols

```yaml
- id: PC-05
  name: Belief re-evaluation
  src: PC ch.5, ch.4
  steps:
    - Name the behavior or feeling and the belief behind it ("Why do I believe I can't?").
    - Ask: is this belief a fact or an assumption/false conclusion?
    - Answer the four questions:
        Q1: Is there a rational reason for this belief?
        Q2: Could I be mistaken?
        Q3: Would I reach the same conclusion about another person in the same situation?
        Q4: Why keep acting as if it were true if there is no good reason to?
    - Engage emotionally (book encourages some indignation at being sold short).
  related: Ideas are replaced by other ideas, not by will. Show the person that a negative self-concept contradicts another belief they hold (Lecky/Russell method).
  data: [belief_text, evidence_for, evidence_against, rewritten_belief]
  maps_to: [IB, TM, PJ]

- id: PC-06
  name: Separate fact from opinion
  src: PC ch.8
  steps: Write the observable fact, then separately write the interpretation you attached to it. Act on the fact.
  maps_to: [TM, PJ]

- id: PC-09
  name: Success memory bank
  src: PC ch.8, ch.14
  rules:
    - Deliberately recall and relive past successes and brave moments, including small ones.
    - Treat errors as information, then drop them; do not replay failures with emotion.
    - Build confidence by graduated successes (start with doable steps and raise gradually).
    - If a person cannot name a success, widen the definition (some successes were not perceived as one's own).
  data: [success_entries(text, feeling)]
  maps_to: [PJ, IB, PAUSE]

- id: PC-10
  name: Winning-feeling protocol and substitution
  src: PC ch.14
  steps:
    - Define the goal as an end result.
    - Recall a past success to capture the feeling, then picture the goal as already achieved with that feeling.
    - "Constructive worry": use the same mental habit as worry but aim it at a positive outcome. Use graded "suppose the best outcome happened" imagery rather than forcing full belief.
  substitution_method: You cannot remove a bad feeling by frontal assault. When a negative feeling appears, immediately call up positive imagery so the negative feeling becomes the cue for the positive state.
  rule: Fear or doubt is not a prediction. Do not "take counsel" of it; act despite it.
  maps_to: [TM, PAUSE, IB]

- id: PC-21
  name: Glance at negatives, focus on the goal
  src: PC ch.9
  rule: Notice warning signals (like a dashboard indicator), act on them, then return attention to the destination. Do not dwell on the signal.
  in_domain: Relapse or craving data is informational. Look, adjust, refocus.
  maps_to: [DAT, PJ]
```

## 3.4 Behavior, urge handling, mood

```yaml
- id: PC-11
  name: Act-as-if and daily commitments (21-day experiment)
  src: PC ch.7
  eight_daily_commitments:
    - Be as cheerful as possible.
    - Act a little friendlier toward others.
    - Be a little less critical and more tolerant; assume the best interpretation of others' actions.
    - Act as if success is inevitable and you already are the person you want to be.
    - Do not color facts pessimistically with your own opinion.
    - Smile at least three times a day.
    - Respond calmly and intelligently whatever happens.
    - Ignore negative "facts" you cannot change.
  anchor_trick: Switch a trivial routine (e.g. which shoe goes on first, how laces are tied) and use it each morning as a reminder for the whole set.
  related:
    - React to threats and problems with positive, goal-oriented action (practice it in imagination too).
    - Close the mind to destructive inputs. Book's anecdote: a boxer stopped reading press coverage that fed his fear (cue-removal logic from AH-08 applies).
  maps_to: [IB, HBB, PJ]

- id: PC-13
  name: Let the telephone ring (urge non-response and delay)
  src: PC ch.12
  model: Disturbing stimuli are "bells" you have been conditioned to obey. The response can be extinguished by relaxing instead of responding.
  steps:
    - Notice the bell (cue). Say the cue is just a ringing phone you do not have to answer.
    - Relax instead of responding.
    - If you cannot ignore it, delay: promise only a short wait (book's example: two minutes), or count slowly to ten with three slow breaths.
  rationale: Delaying breaks the automatic conditioned chain; muscle tension is part of the response, so physical relaxation counts as non-response.
  in_domain: Urge arrives -> delay timer -> re-decide.
  data: [urge_time, delay_seconds, outcome(acted/did_not_act)]
  maps_to: [PAUSE, TM, DAT]

- id: PC-15
  name: Respond to the present only
  src: PC ch.6
  rules:
    - Ask: "What is here and now that I can do something about?" Respond only to that.
    - Do not react to past scenes as if they were present ("straw men from the past").
    - One task at a time (hourglass image: one grain passes at a time; one job into the hopper).
    - Sleep on stuck problems; stop forcing.
  maps_to: [TM, PAUSE]

- id: PC-17
  name: Reinterpret arousal (excitement vs fear)
  src: PC ch.13
  rules:
    - The physiological arousal before a challenge is raw energy; label it excitement and aim it at the goal.
    - Keep the original goal in view instead of switching to escape as the goal.
    - Use an aggressive, goal-directed stance ("I can see this through") rather than a defensive one ("I hope nothing happens").
  maps_to: [TM, PAUSE]

- id: PC-19
  name: Safety valves for pent-up emotional energy
  src: PC ch.9
  options: vigorous physical exercise, brisk walking, hitting/striking sports, goal-directed work, martial/breath-based practices (tai chi, qigong etc.).
  rule: Best channel is progress toward a goal.
  maps_to: [HBB, TM]
  # NOTE: these are active outlets only. Do not use pain/shock-based substitutes (not in the books; not recommended).

- id: PC-18
  name: Forgiveness (others and self)
  src: PC ch.10
  rules:
    - Real forgiveness is complete and then forgotten; remembering it as a favor re-opens the wound.
    - Forgive yourself as well as others. Dwelling on failure feeds the failure-image.
    - A healthy self-image "does not bruise easily"; relaxation cushions emotional blows.
  in_domain: Post-lapse shame handling (pairs with AH-18).
  maps_to: [PJ, TM, IB]

- id: PC-20
  name: Act and correct; stop post-hoc self-criticism
  src: PC ch.10
  rules:
    - Do not over-plan what to say or do; act and correct as you go.
    - Stop the after-the-fact "should I have done that?" loop.
    - Disinhibition practices listed: speak a bit louder than usual; compliment at least three people a day.
  maps_to: [PJ, SS]

- id: PC-08
  name: Success-type vs failure-type dimensions
  src: PC ch.8-9
  success_dimensions: [Sense of direction, Understanding, Courage, Compassion, Esteem, Self-confidence, Self-acceptance]
  failure_dimensions: [Frustration, Aggressiveness (misdirected), Insecurity, Loneliness, Uncertainty, Resentment, Emptiness]
  note: These are descriptive categories the author uses for diagnosis. The book supplies no scored questionnaire.
  maps_to: [IB, PR]  # could seed self-reflection prompts; not a validated assessment

- id: PC-12
  name: Reserve-judgment window
  src: PC preface
  rule: Practice the exercises for about 21 days before judging results; do not argue with the ideas or constantly measure progress during that window.
  caveat: See conflict C-1. Treat as a trial period, not a habit-formation guarantee.
  maps_to: [IB, PJ]
```

# 4. Module -> protocol map

| Prottasha component | Protocols that supply content | What the protocols contribute (data/behavior vocabulary) |
|---|---|---|
| Identity Blueprint | AH-01, AH-23, PC-01, PC-02, PC-03, PC-05, PC-08, PC-09, PC-10, PC-11, PC-16 | Identity statement, "type of person who could get this outcome", daily imagery rehearsal, belief rewrite, success-memory bank |
| Four Laws Engine | AH-02, AH-03, section 2.3 table (rules 1.1-4.6), AH-05..AH-20 | Rule catalogue for build and break, with inversions |
| Digital Addiction Tracker | AH-02, AH-03, AH-11, AH-17, AH-18, AH-19, PC-13, PC-21 | Loop fields (cue/craving/response/reward), motive tag, streak and consecutive-miss counters, urge/delay log, non-numeric wins |
| Trigger Management | AH-02, AH-04, AH-05, AH-06, AH-08, AH-11, AH-12, PC-04, PC-05, PC-06, PC-10, PC-13, PC-14, PC-15, PC-16, PC-17, PC-19 | If-then and stacking plans, craving analysis, delay, relaxation, reframing, rehearsal |
| Habit Builder/Breaker | AH-05, AH-06, AH-07, AH-09, AH-11, AH-13, AH-14, AH-16, AH-18, AH-21, AH-24, PC-11, PC-19 | Replacement habits tied to same motive, two-minute version, habit shaping phases, bundling, friction |
| Social Support | AH-10, AH-15, AH-20, PC-20 | Group-norm joining, partner as lock-holder/observer, contract fields, encouragement behavior |
| Progress Journaling | AH-01, AH-17, AH-18, AH-19, AH-22, PC-05, PC-09, PC-18, PC-20, PC-21 | Vote tally, review questions, belief log, success log, forgiveness and no-dwell rules |
| Professional Referral | PC-08 (as prompts only), section 5 notes | Books supply no clinical protocol; see section 5 |
| Pause screen ("identity or habit?") | AH-01 (decision question), AH-04, AH-12, PC-13, PC-15, PC-17, AX-6 | Point-and-call, delay window, reframe, present-moment question, non-shaming tone |
| Site/app blocking | AH-07, AH-08, AH-13, AH-15 | Friction increase, cue removal, commitment device, third-party lock |
| Sleep mode | AH-07, AH-15, AH-25, PC-04, PC-14 | Bed = sleep only, device out of the bedroom, relaxation imagery |
| Grayscale focus | none | Not covered by either book (see section 5) |

# 5. Gaps, conflicts, and safety notes (read before use)

```yaml
conflicts:
  - id: C-1
    topic: How long a habit takes
    detail: PC preface asks for 21 days of practice before judging; its figure comes from observations like adjustment after plastic surgery and phantom limbs, which are anecdotes, not habit-formation data. AH ch.11 explicitly says there is no magic day count and that repetitions, not days, drive automaticity.
    resolution_hint: Use AH-24 for habit-formation logic. Use PC-12 only as a "keep practicing before judging" trial window.
  - id: C-2
    topic: Forgetting failures vs tracking them
    detail: PC says drop past failures and replay successes. AH tracks every miss and reviews them. Both agree failure is information and not an identity verdict.
    resolution_hint: Log misses as data (AH-17, AH-18, PC-21), do not ruminate on them (PC-09, PC-18).

coverage_gaps:
  - Neither book has protocols for PMO, online gambling, or gaming specifically. Only general habit and cue principles apply.
  - Neither book covers grayscale display, screen-time metrics, or app-level blocking (AH only mentions website blockers and device friction in general terms).
  - Sleep: only AH-25 (bed context) and AH-15 (remove TV, blackout curtains) plus PC relaxation imagery. No sleep schedule or wind-down protocol.
  - Anonymous encouragement, pods, and therapist directory are not in the books. The books support only accountability partners, contracts, and joining a supportive culture.
  - The books give no self-assessment instrument and no criteria for when to refer someone to a professional.

safety_notes:
  - AH ch.12 says the friction tricks are unlikely to curb a true addiction. PC is a 1960s self-help text with a heavily anecdotal evidence base and some religious framing; it is not a clinical treatment. Neither book is a substitute for professional care, so the Professional Referral module needs its own clinical sourcing.
  - AX-6: AH ch.7 reports shame and scare tactics can backfire into the coping habit. Any pause-screen, streak-loss, or relapse copy should avoid shame framing.
  - Streak mechanics: AH-17 recommends "don't break the chain", while AH-18/AH-19 warn about all-or-nothing and Goodhart effects. A broken streak needs a "never miss twice" path, not a punishment.
  - PC-19 lists active outlets only. Do not substitute pain, shock, or self-punishing behaviors; the books do not recommend them.
  - PC-03 and PC-04 involve relaxation and imagery on emotional material. Allow stopping at any time.
```
