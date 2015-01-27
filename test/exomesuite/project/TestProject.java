/*
 * Copyright (C) 2015 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package exomesuite.project;

import exomesuite.ExomeSuite;
import exomesuite.MainViewController;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

/**
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class TestProject {

    ExomeSuite suite = new ExomeSuite();

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void open() {
        File f = new File("/home/unidad03/DNA_Sequencing/exomeSuite/n019/n019.config");
        MainViewController.getWorkingArea();
        try {
            suite.start(null);
        } catch (Exception ex) {
            Logger.getLogger(TestProject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
