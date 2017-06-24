package the.miner;

import org.junit.Test;

import the.miner.engine.database.model.GMImage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ImageTest {


    @Test
    public void addCaption() {
        /* Case 1: add no caption */
        GMImage image = new GMImage();
        assertArrayEquals(new String[]{}, image.addCaption("en"));
        assertEquals("", image.getCaptionString());
        assertEquals(0, image.getCaptions("en").length);
        assertEquals(0, image.getCaptions("vi").length);

        /* Case 2: add one caption */
        assertArrayEquals(new String[]{"Man in black"}, image.addCaption("en", ".Man in black."));
        assertEquals("@en(Man in black)", image.getCaptionString());
        String[] captions = image.getCaptions("en");
        assertEquals(1, captions.length);
        assertEquals("Man in black", captions[0]);

        /* Case 3: add multiple captions and multiple language */
        image = new GMImage();
        assertArrayEquals(new String[]{"Man in black", "Man in white", "Man in transparent"},
                          image.addCaption("en", ".Man in black.", ".Man in white.", "Man in transparent"));
        assertArrayEquals(new String[]{"abc", "xyz"},
                          image.addCaption("vi", "abc", "xyz"));
        assertEquals("@en(Man in black.Man in white.Man in transparent)@vi(abc.xyz)", image.getCaptionString());

        captions = image.getCaptions("en");
        assertEquals(3, captions.length);
        assertEquals("Man in black", captions[0]);
        assertEquals("Man in white", captions[1]);
        assertEquals("Man in transparent", captions[2]);

        captions = image.getCaptions("vi");
        assertEquals(2, captions.length);
        assertEquals("abc", captions[0]);
        assertEquals("xyz", captions[1]);

        /* Case 4: add empty caption */
        image = new GMImage();
        assertArrayEquals(new String[]{"Man in black"}, image.addCaption("en", "Man in black", "", "  "));
        assertEquals("@en(Man in black)", image.getCaptionString());

        /* Case 5: add existing caption */
        image = new GMImage();
        assertArrayEquals(new String[]{"abc", "xyz"}, image.addCaption("en", "abc", "xyz"));
        assertArrayEquals(new String[]{}, image.addCaption("en", "abc"));
        assertEquals("@en(abc.xyz)", image.getCaptionString());

        /* Case 6: add invalid caption */
        image = new GMImage();
        for (char cap : GMImage.CAPSC.toCharArray()) {
            assertArrayEquals(new String[]{}, image.addCaption("en", "" + cap));
            assertEquals("", image.getCaptionString());
        }

        /* Case 7: add caption which is substring of existing captions */
        image = new GMImage();
        assertArrayEquals(new String[]{"I have a dream", "dream"}, image.addCaption("en", "I have a dream", "dream"));
        assertArrayEquals(new String[]{"I have a"}, image.addCaption("en", "I have a"));
        assertEquals("@en(I have a dream.dream.I have a)", image.getCaptionString());

    }

    @Test
    public void removeCaption() {
        GMImage image = new GMImage();
        image.addCaption("en", "Man in black", "Man in white", ".Man in sun");
        image.addCaption("vi", "Man in moon", "Man in nothing.", ".Man in everywhere.");

        /* Case 1: remove one caption*/
        image.removeCaption("en", "Man in white");
        assertEquals("@en(Man in black.Man in sun)@vi(Man in moon.Man in nothing.Man in everywhere)", image.getCaptionString());

        /* Case 4: remove many caption*/
        image.removeCaption("vi", "Man in nothing", "Man in moon");
        assertEquals("@en(Man in black.Man in sun)@vi(Man in everywhere)", image.getCaptionString());

        /* Case 4: remove all caption*/
        image.removeCaption("en", "Man in black", "Man in sun");
        assertEquals("@vi(Man in everywhere)", image.getCaptionString());

        // Remove all caption by lang
        image.removeCaption("vi");
        assertEquals("", image.getCaptionString());
    }

    @Test
    public void addCategory() {
        /* Case 1: add no category */
        GMImage image = new GMImage();
        assertArrayEquals(new String[]{}, image.addCategory("en"));
        assertEquals("", image.getCategoryString());
        assertEquals(0, image.getCategories("en").length);
        assertEquals(0, image.getCategories("vi").length);

        /* Case 2: add one category */
        assertArrayEquals(new String[]{"xyz"}, image.addCategory("en", "#xyz#"));
        assertEquals("@en(#xyz)", image.getCategoryString());
        String[] categories = image.getCategories("en");
        assertEquals(1, categories.length);
        assertEquals("xyz", categories[0]);

        /* Case 3: add multiple categories and multiple language */
        image = new GMImage();
        assertArrayEquals(new String[]{"tree", "abc", "xyz"},
                          image.addCategory("en", "tree", "#abc", "xyz#"));
        assertArrayEquals(new String[]{"dog", "cat"},
                          image.addCategory("vi", "dog", "#cat#"));
        assertEquals("@en(#tree#abc#xyz)@vi(#dog#cat)", image.getCategoryString());

        categories = image.getCategories("en");
        assertEquals(3, categories.length);
        assertEquals("tree", categories[0]);
        assertEquals("abc", categories[1]);
        assertEquals("xyz", categories[2]);

        categories = image.getCategories("vi");
        assertEquals(2, categories.length);
        assertEquals("dog", categories[0]);
        assertEquals("cat", categories[1]);

        /* Case 4: add empty caption */
        image = new GMImage();
        assertArrayEquals(new String[]{"abc"}, image.addCategory("en", "abc", "", "  "));
        assertEquals("@en(#abc)", image.getCategoryString());

        /* Case 5: add existing caption */
        image = new GMImage();
        assertArrayEquals(new String[]{"abc", "xyz"}, image.addCategory("en", "abc", "xyz"));
        assertArrayEquals(new String[]{}, image.addCategory("en", "abc"));
        assertEquals("@en(#abc#xyz)", image.getCategoryString());

        /* Case 6: add invalid categories */
        image = new GMImage();
        for (char cat : GMImage.CSC.toCharArray()) {
            assertArrayEquals(new String[]{}, image.addCategory("en", "" + cat));
            assertEquals("", image.getCategoryString());
        }

        /* Case 7: add caption which is substring of existing captions */
        image = new GMImage();
        assertArrayEquals(new String[]{"orchid flower", "flower"}, image.addCategory("en", "orchid flower", "flower"));
        assertArrayEquals(new String[]{"orchid"}, image.addCategory("en", "orchid"));
        assertEquals("@en(#orchid flower#flower#orchid)", image.getCategoryString());
    }

    @Test
    public void removeCategory() {
        GMImage image = new GMImage();
        image.addCategory("en", "#dog", "cat#", "pig");
        image.addCategory("vi", "abc", "#xyz#", "xxx");

        /* Case 1: remove one caption*/
        image.removeCategory("en", "cat");
        assertEquals("@en(#dog#pig)@vi(#abc#xyz#xxx)", image.getCategoryString());

        /* Case 4: remove many caption*/
        image.removeCategory("vi", "xyz", "#abc");
        assertEquals("@en(#dog#pig)@vi(#xxx)", image.getCategoryString());

        /* Case 4: remove all caption*/
        image.removeCategory("en", "dog", "pig");
        assertEquals("@vi(#xxx)", image.getCategoryString());

        // Remove all caption by lang
        image.removeCategory("vi");
        assertEquals("", image.getCategoryString());
    }
}
