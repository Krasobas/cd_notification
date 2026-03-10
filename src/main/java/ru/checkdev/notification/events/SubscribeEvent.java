package ru.checkdev.notification.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeEvent<T> {
    private T object;
    private Action action;

    public enum Action {
        ADD, DELETE
    }
}