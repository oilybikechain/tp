package mishmash.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mishmash.ui.MealPlanViewer;
import mishmash.ui.MealPlanViewer.MealSummary;

/**
 * A generated meal plan: one {@link DayPlan} for each day, in order.
 */
public class MealPlan {
    private final List<DayPlan> days;
    private final NutritionTargets targets;

    /**
     * Creates a meal plan from the given days.
     *
     * @param days The day plans, in order; copied so later changes do not leak in.
     * @throws NullPointerException If the list or any day is null.
     */
    public MealPlan(List<DayPlan> days) {
        this(days, NutritionTargets.none());
    }

    /**
     * Creates a meal plan retaining the daily targets used during generation.
     *
     * @param days The day plans, in order; copied to prevent later changes.
     * @param targets The immutable daily targets, including any omitted nutrients.
     * @throws NullPointerException If the list, any day, or the targets are null.
     */
    public MealPlan(List<DayPlan> days, NutritionTargets targets) {
        Objects.requireNonNull(days, "days");
        this.days = List.copyOf(days);
        this.targets = Objects.requireNonNull(targets, "targets");
    }

    /**
     * Returns the daily targets supplied when this plan was generated.
     *
     * @return The plan's targets, with no targets set for an untargeted plan.
     */
    public NutritionTargets getTargets() {
        return targets;
    }

    /**
     * Returns an unmodifiable list of the plan's days.
     */
    public List<DayPlan> getDays() {
        return days;
    }

    /**
     * Returns how many days the plan covers.
     */
    public int getNumberOfDays() {
        return days.size();
    }

    /**
     * Converts the plan to the display data that {@link MealPlanViewer} expects.
     * The viewer is deliberately independent of how recipes and plans are stored,
     * so this method is the single place that adapts the two.
     *
     * @return The meals of each day, where the first list represents day 1.
     */
    public List<List<MealSummary>> toMealSummaries() {
        List<List<MealSummary>> mealsByDay = new ArrayList<>();
        for (DayPlan day : days) {
            List<MealSummary> summaries = new ArrayList<>();
            for (Recipe meal : day.getMeals()) {
                summaries.add(new MealSummary(meal.getName(), meal.getCalories(),
                        meal.getProtein(), meal.getCarbs(), meal.getFats()));
            }
            mealsByDay.add(summaries);
        }
        return mealsByDay;
    }
}
