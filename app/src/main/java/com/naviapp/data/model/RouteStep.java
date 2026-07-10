package com.naviapp.data.model;

/**
 * Один шаг пошаговой навигации: "Поверните направо на ул. Ленина через 200 м".
 */
public class RouteStep {

    private final String instruction;   // Человекочитаемый текст инструкции
    private final String maneuverType;  // turn / merge / roundabout / arrive и т.д.
    private final String maneuverModifier; // left / right / straight и т.д.
    private final double distanceMeters;   // Расстояние до конца этого шага
    private final GeoPoint location;       // Координата маневра

    public RouteStep(String instruction, String maneuverType, String maneuverModifier,
                      double distanceMeters, GeoPoint location) {
        this.instruction = instruction;
        this.maneuverType = maneuverType;
        this.maneuverModifier = maneuverModifier;
        this.distanceMeters = distanceMeters;
        this.location = location;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getManeuverType() {
        return maneuverType;
    }

    public String getManeuverModifier() {
        return maneuverModifier;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public GeoPoint getLocation() {
        return location;
    }
}
