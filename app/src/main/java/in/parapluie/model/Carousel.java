package in.parapluie.model;

/**
 * Created by surbhimanurkar on 19-05-2016.
 * This is the basic building block of a story. Many story atoms can form part of a carousel.
 */
public class Carousel {
    private String text;
    private String image;
    private int position;

    public Carousel(){

    }

    public Carousel(String text, String image, int position) {
        this.text = text;
        this.image = image;
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
