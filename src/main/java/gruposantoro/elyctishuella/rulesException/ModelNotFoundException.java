package gruposantoro.elyctishuella.rulesException;


public class ModelNotFoundException  extends Exception{
    public ModelNotFoundException(Class<?> model) {
        super(model.getSimpleName() + " not found");
    }

    public ModelNotFoundException(Class<?> model, Long id) {
        super(model.getSimpleName() + " not found with id " + id);
    }

    public ModelNotFoundException(Class<?> model, String id) {
        super(model.getSimpleName() + " not found with passportNumber equals to " + id);
    }

}
