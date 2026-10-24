# Developer Guide

## Acknowledgements

{list here sources of all reused/adapted ideas, code, documentation, and third-party libraries -- include links to the original source as well}

## Design & implementation

### Nutrition target detection

`MealPlan` retains the immutable `NutritionTargets` supplied to targeted generation. Its existing
one-argument constructor defaults to `NutritionTargets.none()`, preserving callers that supply only days.
The new constructor accepts days and targets; `getTargets()` exposes the retained targets. Random generation
has no targets. Storing targets in the plan keeps replacement and failed-generation behavior consistent:
successful generation replaces meals and targets together, while failure leaves the previous plan intact.

`NutritionTargets.calculateExcesses(NutritionTotals)` returns each nutrient's positive excess over its exact
target, or zero for an omitted, equal, or higher target. It uses `long` arithmetic, matching daily totals,
so sums beyond the integer range remain valid. This comparison deliberately does not use `TOLERANCE`:
the generator still accepts days within ±15%, while viewing warns for any total strictly above its target.

`Parser` supplies the current plan's meals and targets to `ViewPlanCommand` at execution time.
Commands parsed before generation or replacement therefore display the latest plan and its targets.
Legacy supplier-based parser and command constructors default to no targets. A new command constructor
accepts meal and target suppliers plus a nullable day number (null selects every day).
`MealPlanViewer` provides target-aware overloads for all days and one selected day, while existing overloads
delegate with no targets. It calculates daily totals, requests excesses, and places warnings before each
day's closing separator in calories, protein, carbohydrates, and fats order. Warnings are informational;
they do not mutate the plan or affect grocery generation. No persistence or command syntax changes are needed.

Unit tests cover exact comparison boundaries, omitted targets, simultaneous excesses, large totals,
and exact display formatting and day selection. Parser integration tests use three recipes, making their
daily totals deterministic, to verify tolerance versus warning behavior and target replacement after success
or failure. Run `gradlew.bat test checkstyleMain checkstyleTest` using Java 25.

### Grocery command integration

`Parser()` shares one `ActiveMealPlan` between meal-plan generation, grocery generation, and viewing.
For deterministic tests or an externally managed plan, use `Parser(MealPlanGenerator, ActiveMealPlan)`.
The supplier-based constructors retain their caller-provided viewing sources; their grocery viewers
do not automatically read the internally generated grocery snapshot.

The parser accepts `generate-list` without arguments and returns `GenerateListCommand` without
reading or modifying the plan. During execution, the command traverses `MealPlan.getDays()`,
`DayPlan.getMeals()`, and each recipe's ingredients. Every meal occurrence contributes its quantities.
The existing `GroceryListGenerator` combines exact name/unit matches with `BigDecimal` arithmetic,
preserving first-appearance order and keeping incompatible units separate.

`ActiveMealPlan.setGroceryList` copies the completed result into a read-only snapshot.
`getGroceryList` returns an optional: absence means no list has been generated for the current plan,
whereas a present empty list is valid. `ViewListCommand` reads this snapshot during execution and
delegates formatting to `GroceryListViewer`; viewing never regenerates data.

Successfully storing a replacement plan clears its old groceries. Failed plan generation or invalid
command syntax leaves the previous state intact. All state is kept in memory for one session.

Grocery parsing and viewing use `FINE` logs, successful generation logs counts at `INFO`, and rejected
generation requests use `WARNING`. These loggers inherit the existing application configuration;
no additional handlers are installed. New diagnostics exclude ingredient names and raw input.

Run `gradlew.bat test checkstyleMain checkstyleTest` on Windows with Java 25 to verify the integration.

{Describe the design and implementation of the product. Use UML diagrams and short code snippets where applicable.}


## Product scope
### Target user profile

{Describe the target user profile}

### Value proposition

{Describe the value proposition: what problem does it solve?}

## User Stories

|Version| As a ... | I want to ... | So that I can ...|
|--------|----------|---------------|------------------|
|v1.0|new user|see usage instructions|refer to them when I forget how to use the application|
|v2.0|user|find a to-do item by name|locate a to-do without having to go through the entire list|

## Non-Functional Requirements

{Give non-functional requirements}

## Glossary

* *glossary item* - Definition

## Instructions for manual testing

### Nutrition target warnings

In a fresh session, enter:

```text
add-recipe n/A cal/500 p/20 c/50 f/10 i/Rice:100g
add-recipe n/B cal/600 p/30 c/60 f/15 i/Rice:100g
add-recipe n/C cal/700 p/40 c/70 f/20 i/Rice:100g
generate-plan d/2 cal/1700 p/85 c/180 f/45
view-plan
view-plan d/2
```

Each day must total 1800 kcal, 90 g protein, 180 g carbohydrates, and 45 g fats, regardless of meal order.
Expect exactly these warnings below each displayed day's totals:

```text
Warning: Calories exceed target by 100 kcal (1800 kcal actual; 1700 kcal target).
Warning: Protein exceeds target by 5 g (90 g actual; 85 g target).
```

`view-plan d/2` must show only day 2. Then try `generate-plan d/1 cal/1`: generation must fail and
`view-plan` must still display the previous two days and their warnings. Finally run `generate-plan d/1`
and `view-plan`: the replacement must show one day with no warnings because no targets were supplied.

{Give instructions on how to do a manual product testing e.g., how to load sample data to be used for testing}
