package stream;

/**
 * @author JellyfishMIX
 * @date 2021/3/31 22:59
 */
public class Event<T> {
    T data;

    public Event(T data) {
        this.data = data;
    }

    static class EventData {
        Integer id;
        String msg;

        public EventData(Integer id, String msg) {
            this.id = id;
            this.msg = msg;
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "data=" + data +
                '}';
    }

    static class Transforms {
        static EventData transform(Integer id) {
            switch (id) {
                case 0:
                    return new EventData(id, "Start");
                case 1:
                    return new EventData(id, "Running");
                case 2:
                    return new EventData(id, "Done");
                case 3:
                    return new EventData(id, "Fail");
                default:
                    return new EventData(id, "Error");
            }
        }
    }

}
