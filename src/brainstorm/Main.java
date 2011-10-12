package brainstorm;

import java.util.ArrayList;

import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.sql.*;

/**
 *
 * @author Jon Doud
 */
public class Main extends QMainWindow {
	// TODO: create executable jar
	// TODO: send updated code home for checkin to svn
    public static void main(String[] args) {
        QApplication.initialize(args);
        Main mainw = new Main();
        mainw.show();
        QApplication.exec();
    }

    Ui_MainWindow mainWindowUi = new Ui_MainWindow();
    QSqlDatabase db;
    QSqlTableModel categoriesModel;
    QSqlTableModel notesModel;
    int currentNote = -1;
    
    public Main() {
        // Create main window and set splitter sizes
        mainWindowUi.setupUi(this);
        ArrayList<Integer> sizes = new ArrayList<Integer>();
        sizes.add(200);
        sizes.add(150);
		mainWindowUi.splitterCategories.setSizes(sizes);
		sizes.set(0, 100);
		sizes.set(1, 250);
        mainWindowUi.splitterNotes.setSizes(sizes);
        
        // set text editor defaults
        mainWindowUi.textNote.document().setIndentWidth(20);
        mainWindowUi.textNote.setTabStopWidth(20);
        mainWindowUi.textNote.setTabChangesFocus(false);
        mainWindowUi.actionIncrease_Indent.setShortcut("Tab");
        mainWindowUi.actionDecrease_Indent.setShortcut("Backtab");
        
        // Set icons
        setWindowIcon(new QIcon("resources/Brainstorm_48x48x32.png"));
        mainWindowUi.actionNew_Note.setIcon(new QIcon("resources/document-new.png"));
        mainWindowUi.actionNew_Category.setIcon(new QIcon("resources/folder-new.png"));
        mainWindowUi.actionSave_Note.setIcon(new QIcon("resources/document-save.png"));
        mainWindowUi.actionUndo.setIcon(new QIcon("resources/edit-undo.png"));
        mainWindowUi.actionRedo.setIcon(new QIcon("resources/edit-redo.png"));
        mainWindowUi.actionCut.setIcon(new QIcon("resources/edit-cut.png"));
        mainWindowUi.actionCopy.setIcon(new QIcon("resources/edit-copy.png"));
        mainWindowUi.actionPaste.setIcon(new QIcon("resources/edit-paste.png"));
        mainWindowUi.actionBold.setIcon(new QIcon("resources/format-text-bold.png"));
        mainWindowUi.actionItalic.setIcon(new QIcon("resources/format-text-italic.png"));
        mainWindowUi.actionUnderline.setIcon(new QIcon("resources/format-text-underline.png"));
        mainWindowUi.actionStrikethrough.setIcon(new QIcon("resources/format-text-strikethrough.png"));
        mainWindowUi.actionShow_Colors.setIcon(new QIcon("resources/format-font-color.png"));
        mainWindowUi.actionBullet_List.setIcon(new QIcon("resources/list-bullets.png"));
        mainWindowUi.actionNumeric_List.setIcon(new QIcon("resources/list-numbered.png"));
        mainWindowUi.actionIncrease_Indent.setIcon(new QIcon("resources/format-indent-more.png"));
        mainWindowUi.actionDecrease_Indent.setIcon(new QIcon("resources/format-indent-less.png"));
        mainWindowUi.actionLeft.setIcon(new QIcon("resources/format-justify-left.png"));
        mainWindowUi.actionRight.setIcon(new QIcon("resources/format-justify-right.png"));
        mainWindowUi.actionCenter.setIcon(new QIcon("resources/format-justify-center.png"));
        mainWindowUi.actionFull.setIcon(new QIcon("resources/format-justify-fill.png"));
        mainWindowUi.actionAbout_Brainstorm.setIcon(new QIcon("resources/brainstorm.png"));
        
        // Setup comboColors
    	QPixmap pix = new QPixmap(16, 16);
    	pix.fill(new QColor(Qt.GlobalColor.white));
    	mainWindowUi.comboFontColors.addItem(new QIcon(pix), "");
    	pix.fill(new QColor(Qt.GlobalColor.black));
    	mainWindowUi.comboFontColors.addItem(new QIcon(pix), "");
    	pix.fill(new QColor(Qt.GlobalColor.red));
    	mainWindowUi.comboFontColors.addItem(new QIcon(pix), "");
    	pix.fill(new QColor(Qt.GlobalColor.blue));
    	mainWindowUi.comboFontColors.addItem(new QIcon(pix), "");
    	pix.fill(new QColor(Qt.GlobalColor.darkGreen));
    	mainWindowUi.comboFontColors.addItem(new QIcon(pix), "");
    	pix.fill(new QColor(Qt.GlobalColor.gray));
    	mainWindowUi.comboFontColors.addItem(new QIcon(pix), "");
    	
    	// Move combo boxes to toolbar
    	mainWindowUi.toolBar.addWidget(mainWindowUi.comboFonts);
    	mainWindowUi.toolBar.addWidget(mainWindowUi.comboFontSizes);
    	mainWindowUi.toolBar.addWidget(mainWindowUi.comboFontColors);
        
        // Connect to database
        connectToDatabase();
        createModels();
        mainWindowUi.listCategories.selectionModel().setCurrentIndex(categoriesModel.index(0, 1), QItemSelectionModel.SelectionFlag.Clear, QItemSelectionModel.SelectionFlag.SelectCurrent);
        selectCategory();
        
        
        // Connect file menu actions
        mainWindowUi.actionNew_Note.triggered.connect(this, "newNote()");
        mainWindowUi.actionSave_Note.triggered.connect(this, "saveNoteText()");
        mainWindowUi.actionDelete_Note.triggered.connect(this, "deleteNote()");
        mainWindowUi.actionNew_Category.triggered.connect(this, "newCategory()");
        mainWindowUi.actionDelete_Category.triggered.connect(this, "deleteCategory()");
        mainWindowUi.actionPrint_Note.triggered.connect(this, "printNote()");
        mainWindowUi.actionImport_Category.triggered.connect(this, "importCategory()");
        mainWindowUi.actionExport_Category.triggered.connect(this, "exportCategory()");
        mainWindowUi.actionQuit.triggered.connect(this, "closeApp()");
        
        // Connect edit menu actions
        mainWindowUi.actionUndo.triggered.connect(this, "undo()");
        mainWindowUi.actionRedo.triggered.connect(this, "redo()");
        mainWindowUi.actionCut.triggered.connect(this, "cut()");
        mainWindowUi.actionCopy.triggered.connect(this, "copy()");
        mainWindowUi.actionPaste.triggered.connect(this, "paste()");
        
        // Connect format menu actions
        mainWindowUi.actionBold.triggered.connect(this, "bold()");
        mainWindowUi.actionItalic.triggered.connect(this, "italic()");
        mainWindowUi.actionUnderline.triggered.connect(this, "underline()");
        mainWindowUi.actionStrikethrough.triggered.connect(this, "strikethrough()");
        mainWindowUi.actionLeft.triggered.connect(this, "justifyLeft()");
        mainWindowUi.actionCenter.triggered.connect(this, "justifyCenter()");
        mainWindowUi.actionRight.triggered.connect(this, "justifyRight()");
        mainWindowUi.actionFull.triggered.connect(this, "justifyFull()");
        mainWindowUi.actionBullet_List.triggered.connect(this, "bulletList()");
        mainWindowUi.actionNumeric_List.triggered.connect(this, "numericList()");
        mainWindowUi.actionIncrease_Indent.triggered.connect(this, "increaseIndent()");
        mainWindowUi.actionDecrease_Indent.triggered.connect(this, "decreaseIndent()");
        mainWindowUi.actionShow_Colors.triggered.connect(this, "showColors()");
        mainWindowUi.actionShow_Fonts.triggered.connect(this, "showFonts()");
        mainWindowUi.actionIncrease_Font.triggered.connect(this, "increaseFont()");
        mainWindowUi.actionDecrease_Font.triggered.connect(this, "decreaseFont()");
        mainWindowUi.actionReset_Font.triggered.connect(this, "resetFont()");
        
        // Connect view menu actions
        mainWindowUi.actionNext_Category.triggered.connect(this, "nextCategory()");
        mainWindowUi.actionPrevious_Category.triggered.connect(this, "previousCategory()");
        mainWindowUi.actionNext_Note.triggered.connect(this, "nextNote()");
        mainWindowUi.actionPrevious_Note.triggered.connect(this, "previousNote()");
        
        // Connect help menu actions
        mainWindowUi.actionAbout_Brainstorm.triggered.connect(this, "aboutBrainstorm()");
        mainWindowUi.actionAbout_QtJambi.triggered.connect(this, "aboutQtJambi()");
        
        // Connect Category list actions
        mainWindowUi.listCategories.selectionModel().currentChanged.connect(this, "selectCategory()");
        // TODO: add right-click menu for Categories
        // Connect Note list actions
        mainWindowUi.listNotes.selectionModel().currentChanged.connect(this, "selectNote()");
        // TODO: add right-click menu for Notes
        // Connect Editor actions
        mainWindowUi.textNote.cursorPositionChanged.connect(this, "updateMenus()");
        mainWindowUi.textNote.currentCharFormatChanged.connect(this, "updateMenus()");
        mainWindowUi.textNote.installEventFilter(this);
        
        // Connect toolbar actions
        mainWindowUi.comboFonts.activated.connect(this, "setFont()");
        mainWindowUi.comboFontSizes.activated.connect(this, "setFontSize()");
        mainWindowUi.comboFontColors.activated.connect(this, "setFontColor()");
    }
	///////////////////////////////////////////////////
	// File Menu Methods
    ///////////////////////////////////////////////////  
    public void newNote() {
    	QSqlRecord r = notesModel.record();
    	r.setNull("id");
    	r.setValue("category_id", categoriesModel.record(mainWindowUi.listCategories.selectionModel().currentIndex().row()).value("id"));
    	r.setValue("name", "A New Note");
    	notesModel.insertRecord(0, r);
    	notesModel.submitAll();
    	mainWindowUi.listNotes.selectionModel().setCurrentIndex(notesModel.index(0, 1), QItemSelectionModel.SelectionFlag.Clear, QItemSelectionModel.SelectionFlag.SelectCurrent);    	
    }
    public void deleteNote() {
    	if(QMessageBox.question(this, "Delete Note", "Do you want to delete the note: "
				 + mainWindowUi.listNotes.selectionModel().currentIndex().data().toString() + "?",
				 QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) == QMessageBox.StandardButton.Yes.value()) {
			notesModel.removeRow(mainWindowUi.listNotes.selectionModel().currentIndex().row());
			notesModel.submitAll();
			mainWindowUi.listNotes.selectionModel().setCurrentIndex(notesModel.index(0, 1), QItemSelectionModel.SelectionFlag.Clear,  QItemSelectionModel.SelectionFlag.SelectCurrent);
	    }
    }
    public void newCategory() {
    	QSqlRecord r = categoriesModel.record();
    	r.setNull("id");
    	r.setValue("name", "A New Category");
    	categoriesModel.insertRecord(0, r);
    	categoriesModel.submitAll();
    	mainWindowUi.listCategories.selectionModel().setCurrentIndex(categoriesModel.index(0, 1), QItemSelectionModel.SelectionFlag.Clear, QItemSelectionModel.SelectionFlag.SelectCurrent);    	
    }
    public void deleteCategory() {
    	if(notesModel.rowCount() > 0) {
    		QMessageBox.warning(this, "Category Deletion Cancelled", "All notes within a category must be deleted\nbefore a category can be deleted.");
    	} else {
    		if(QMessageBox.question(this, "Delete Category", "Do you want to delete the category: "
    								 + mainWindowUi.listCategories.selectionModel().currentIndex().data().toString() + "?",
    								 QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) == QMessageBox.StandardButton.Yes.value()) {
    			categoriesModel.removeRow(mainWindowUi.listCategories.selectionModel().currentIndex().row());
    			categoriesModel.submitAll();
    			mainWindowUi.listCategories.selectionModel().setCurrentIndex(categoriesModel.index(0, 1), QItemSelectionModel.SelectionFlag.Clear, QItemSelectionModel.SelectionFlag.SelectCurrent);
    		}
    	}
    }
    
    public void printNote() {
    	// TODO: implement printNote method    	
    }
    public void importCategory() {
    	// TODO: implement importCategory method    	
    }
    public void exportCategory() {
    	// TODO: implement exportCategory method
    }
    
    public void closeApp() {
    	saveNoteText();
    	closeDatabase();
    	QApplication.quit();
    }
    
    @Override
    public void closeEvent(QCloseEvent event) {
    	event.ignore();
    	closeApp();
    }
    
   ///////////////////////////////////////////////////
   // Edit Menu Methods
   ///////////////////////////////////////////////////
   public void undo() {
	   if(mainWindowUi.textNote.undoAvailable != null)
		   mainWindowUi.textNote.undo();
   }
   public void redo() {
	   if(mainWindowUi.textNote.redoAvailable != null)
		   mainWindowUi.textNote.redo();
   }
   public void cut() {
	   mainWindowUi.textNote.cut();
   }
   public void copy() {
	   mainWindowUi.textNote.copy();
   }
   public void paste() {
	   if(mainWindowUi.textNote.canPaste())
		   mainWindowUi.textNote.paste();
   }
   
   ///////////////////////////////////////////////////
   // Format Menu Methods
   ///////////////////////////////////////////////////
   public void bold() {
	   if(mainWindowUi.textNote.fontWeight() == QFont.Weight.Normal.value())
		   mainWindowUi.textNote.setFontWeight(QFont.Weight.Bold.value());
	   else
		   mainWindowUi.textNote.setFontWeight(QFont.Weight.Normal.value());
		   
   }  
   public void italic() {
	   mainWindowUi.textNote.setFontItalic(!mainWindowUi.textNote.fontItalic());
   }
   public void underline() {
	   mainWindowUi.textNote.setFontUnderline(!mainWindowUi.textNote.fontUnderline());
   }
   public void strikethrough() {
	   QFont font = mainWindowUi.textNote.currentFont();
	   font.setStrikeOut(!font.strikeOut());
	   mainWindowUi.textNote.setCurrentFont(font);
   }
   
   public void justifyLeft() {
	   mainWindowUi.textNote.setAlignment(Qt.AlignmentFlag.AlignLeft);
	   updateMenus();
   }
   public void justifyCenter() {
	   mainWindowUi.textNote.setAlignment(Qt.AlignmentFlag.AlignCenter);
	   updateMenus();
   }
   public void justifyRight() {
	   mainWindowUi.textNote.setAlignment(Qt.AlignmentFlag.AlignRight);
	   updateMenus();
   }
   public void justifyFull() {
	   mainWindowUi.textNote.setAlignment(Qt.AlignmentFlag.AlignJustify);
	   updateMenus();
   }
   
   public void bulletList() {
	   toggleList(QTextListFormat.Style.ListDisc);
   }
   public void numericList() {
	   toggleList(QTextListFormat.Style.ListDecimal);
   }
   @SuppressWarnings("null")
   public void toggleList(QTextListFormat.Style style) {
		QTextCursor cursor = mainWindowUi.textNote.textCursor();
		QTextBlockFormat blockFmt = cursor.blockFormat();
		QTextListFormat listFmt = new QTextListFormat();
		
		boolean list = (cursor.currentList() != null);
		
		// change style if list exists and is a different style
		if(list && (cursor.currentList().textListFormat().style() != style)) {
			listFmt.setStyle(style);
			cursor.currentList().setFormat(listFmt);
		}
		// remove list if exists and matches style
		else if(list && (cursor.currentList().textListFormat().style() == style)) {
			cursor.currentList().removeItem(0);
			blockFmt = mainWindowUi.textNote.textCursor().blockFormat();
			cursor = mainWindowUi.textNote.textCursor();
			blockFmt.setIndent(0);
			cursor.setBlockFormat(blockFmt);
		// create list if not exists
		} else if (!list) {
			cursor.beginEditBlock();
			listFmt.setIndent(blockFmt.indent() + 1);
			blockFmt.setIndent(0);
			cursor.setBlockFormat(blockFmt);
			listFmt.setStyle(style);
			cursor.createList(listFmt);
			cursor.endEditBlock();
		}
		updateMenus();
   }
   
   public void increaseIndent() {
	   QTextBlockFormat blockFmt = mainWindowUi.textNote.textCursor().blockFormat();
	   QTextCursor cursor = mainWindowUi.textNote.textCursor();

	   blockFmt.setIndent(blockFmt.indent()+1);
	   cursor.setBlockFormat(blockFmt);
   }
   public void decreaseIndent() {
	   QTextBlockFormat blockFmt = mainWindowUi.textNote.textCursor().blockFormat();
	   QTextCursor cursor = mainWindowUi.textNote.textCursor();

	   if(blockFmt.indent() > 0)
		   blockFmt.setIndent(blockFmt.indent()-1);
	   else
		   blockFmt.setIndent(0);
	   cursor.setBlockFormat(blockFmt);
   }
   
   public void showColors() {
	   QColor color = QColorDialog.getColor();
	   if(color.isValid()) {
		   mainWindowUi.textNote.setTextColor(color);
	   }
   }
   public void showFonts() {
	   QFontDialog.Result result = QFontDialog.getFont(mainWindowUi.textNote.currentFont());
	   if(result.ok) {
		   mainWindowUi.textNote.setCurrentFont(result.font);
	   }
   }
   
   public void setFont() { 
	   mainWindowUi.textNote.setFontFamily(mainWindowUi.comboFonts.currentText());
	   mainWindowUi.textNote.setFocus();
   }
   public void setFontSize() {
	   mainWindowUi.textNote.setFontPointSize(Integer.parseInt(mainWindowUi.comboFontSizes.currentText()));
	   mainWindowUi.textNote.setFocus();
   }
   public void setFontColor() {
	   switch (mainWindowUi.comboFontColors.currentIndex())
		{
			case 1:
				mainWindowUi.textNote.setTextColor(new QColor(Qt.GlobalColor.black));
				break;
			case 2:
				mainWindowUi.textNote.setTextColor(new QColor(Qt.GlobalColor.red));
				break;
			case 3:
				mainWindowUi.textNote.setTextColor(new QColor(Qt.GlobalColor.blue));
				break;
			case 4:
				mainWindowUi.textNote.setTextColor(new QColor(Qt.GlobalColor.darkGreen));
				break;
			case 5:
				mainWindowUi.textNote.setTextColor(new QColor(Qt.GlobalColor.gray));
				break;
		}
	   mainWindowUi.textNote.setFocus();
   }
   public void increaseFont() {
	   mainWindowUi.comboFontSizes.activated.disconnect();
	   
		// step .5 up to 8
		// step 1.0 up to 14
		// step 2.0 up to 30
		// step 6 over 30
		double increment = 0.0f;
		if (mainWindowUi.textNote.currentFont().pointSizeF() < 8)
			increment = 0.5f;
		else if (mainWindowUi.textNote.currentFont().pointSizeF() < 14)
			increment = 1.0f;
		else if (mainWindowUi.textNote.currentFont().pointSizeF() <= 29)
			increment = 2.0f;
		else
			increment = 6.0f;

		mainWindowUi.textNote.setFontPointSize(mainWindowUi.textNote.currentFont().pointSizeF() + increment);

		mainWindowUi.comboFontSizes.activated.connect(this, "setFontSize()");
   }
   public void decreaseFont() {
	   mainWindowUi.comboFontSizes.activated.disconnect();
	   
		// step .5 up to 8
		// step 1.0 up to 14
		// step 2.0 up to 30
		// step 6 over 32
		double decrement = 0.0f;
		if (mainWindowUi.textNote.currentFont().pointSizeF() <= 8)
			decrement = 0.5f;
		else if (mainWindowUi.textNote.currentFont().pointSizeF() <= 14)
			decrement = 1.0f;
		else if (mainWindowUi.textNote.currentFont().pointSizeF() <= 32)
			decrement = 2.0f;
		else
			decrement = 6.0f;

		mainWindowUi.textNote.setFontPointSize(mainWindowUi.textNote.currentFont().pointSizeF() - decrement);

		mainWindowUi.comboFontSizes.activated.connect(this, "setFontSize()");
   }
   public void resetFont() {
	   int pos = mainWindowUi.textNote.textCursor().position();
	   mainWindowUi.textNote.selectAll();
	   mainWindowUi.textNote.setFontFamily("Calibri");
	   mainWindowUi.textNote.setFontPointSize(11);
	   mainWindowUi.textNote.setTextColor(new QColor(Qt.GlobalColor.black));
	   QTextCursor textCursor = mainWindowUi.textNote.textCursor();
	   textCursor.clearSelection();
	   textCursor.setPosition(pos);
	   mainWindowUi.textNote.setTextCursor( textCursor );
	   mainWindowUi.textNote.setFocus();
   }
   
	//////////////////////////////
	// View Menu actions
	//////////////////////////////
	void nextCategory()
	{
		if(mainWindowUi.listCategories.selectionModel().currentIndex().row() < mainWindowUi.listCategories.selectionModel().model().rowCount()-1)
		{
			saveNoteText();
			int row = mainWindowUi.listCategories.selectionModel().currentIndex().row()+1;
			mainWindowUi.listCategories.selectionModel().setCurrentIndex(categoriesModel.index(row, 1), QItemSelectionModel.SelectionFlag.SelectCurrent);
			updateMenus();
			mainWindowUi.textNote.setFocus();
		}
	}
	void previousCategory()
	{
		if(mainWindowUi.listCategories.selectionModel().currentIndex().row() >0)
		{
			saveNoteText();
			int row = mainWindowUi.listCategories.selectionModel().currentIndex().row()-1;
			mainWindowUi.listCategories.selectionModel().setCurrentIndex(categoriesModel.index(row, 1), QItemSelectionModel.SelectionFlag.SelectCurrent);
			updateMenus();
			mainWindowUi.textNote.setFocus();
		}
	}
	void nextNote()
	{
		if(currentNote < mainWindowUi.listNotes.selectionModel().model().rowCount()-1)
		{
			saveNoteText();
			int row = currentNote+1;
			mainWindowUi.listNotes.selectionModel().setCurrentIndex(notesModel.index(row, 2), QItemSelectionModel.SelectionFlag.SelectCurrent);
			updateMenus();
			mainWindowUi.textNote.setFocus();
		}
	}
	void previousNote()
	{
		if(currentNote > 0)
		{
			saveNoteText();
			int row = currentNote-1;
			mainWindowUi.listNotes.selectionModel().setCurrentIndex(notesModel.index(row, 2), QItemSelectionModel.SelectionFlag.SelectCurrent);
			updateMenus();
			mainWindowUi.textNote.setFocus();
		}
	}
   
   ///////////////////////////////////////////////////
   // Help Menu Methods
   ///////////////////////////////////////////////////
   public void aboutBrainstorm() {
	   QMessageBox.about(this, "Brainstorm", "A simple app to track random thoughts.");
   }
   
   public void aboutQtJambi() {
	   QMessageBox.aboutQt(this);
   }
   
   ///////////////////////////////////////////////////
   // Database Methods
   ///////////////////////////////////////////////////
   private void connectToDatabase() {
       db = QSqlDatabase.addDatabase("QSQLITE", "qt_sql_default_connection");
       db.setDatabaseName("brainstorm.sqlite");
       if (!db.open()) {
           System.out.println("Connection Failed!");
           System.out.println(db.lastError().text());
           return;
       }
   }
   
   private void createModels() {
	   categoriesModel = new QSqlTableModel();
	   categoriesModel.setTable("categories");
	   categoriesModel.setSort(1, Qt.SortOrder.AscendingOrder);
	   categoriesModel.select();
	   mainWindowUi.listCategories.setModel(categoriesModel);
	   mainWindowUi.listCategories.setModelColumn(1);
	   mainWindowUi.listCategories.show();
	   
	   notesModel = new QSqlTableModel();
	   notesModel.setTable("notes");
	   mainWindowUi.listNotes.setModel(notesModel);
	   mainWindowUi.listNotes.setModelColumn(2);
	   mainWindowUi.listNotes.show();
   }
   
   public void saveNoteText() {
	   if(currentNote != -1) {
		  	String id = notesModel.record(currentNote).value("id").toString();
			String text = mainWindowUi.textNote.document().toHtml();
			text = text.replace("'", "''");
			String queryString = "update notes set note_text = '" + text + "' where id = " + id;
			QSqlQuery query = new QSqlQuery(queryString);
			query.exec();
			notesModel.submitAll();
			notesModel.select();
	   }
   }
   
   private void closeDatabase() {
	   db.disconnect();
	   db.close();
   }

   ///////////////////////////////////////////////////
   // ListView Methods
   ///////////////////////////////////////////////////
   public void selectCategory() {
	   saveNoteText();
	   if(mainWindowUi.listCategories.selectionModel().currentIndex() == null) {
		   mainWindowUi.listCategories.selectionModel().setCurrentIndex(categoriesModel.index(0, 2), QItemSelectionModel.SelectionFlag.SelectCurrent);
		   mainWindowUi.textNote.setText("");
	   } else {
		   notesModel.setFilter("category_id=" + categoriesModel.record(mainWindowUi.listCategories.selectionModel().currentIndex().row()).value("id").toString());
		   notesModel.setSort(2, Qt.SortOrder.AscendingOrder);
		   notesModel.select();
		   mainWindowUi.listNotes.selectionModel().setCurrentIndex(notesModel.index(0, 2), QItemSelectionModel.SelectionFlag.SelectCurrent);
	   }
	   selectNote();
   }
   
   public void selectNote() {
	   if(notesModel.rowCount() == 0)
		{
			currentNote = -1;
			mainWindowUi.textNote.setText("");
		}
	   else if(mainWindowUi.listNotes.selectionModel().currentIndex() != null)
		{
			currentNote = mainWindowUi.listNotes.selectionModel().currentIndex().row();
			mainWindowUi.textNote.setText(notesModel.record(currentNote).value("note_text").toString());
		}
	   mainWindowUi.textNote.setFocus();
   }
   
   ///////////////////////////////////////////////////
   // Application Methods
   ///////////////////////////////////////////////////
   public void updateMenus() { 
	   // delete note menu
	   mainWindowUi.actionDelete_Note.setEnabled(mainWindowUi.listNotes.selectionModel().currentIndex() != null);
	   
	   // paste menu
	   mainWindowUi.actionPaste.setEnabled(mainWindowUi.textNote.canPaste() && mainWindowUi.listNotes.selectionModel().currentIndex() != null);
	   
	   // current font style
	   mainWindowUi.actionBold.setChecked(mainWindowUi.textNote.fontWeight() == QFont.Weight.Bold.value());
	   mainWindowUi.actionItalic.setChecked(mainWindowUi.textNote.fontItalic());
	   mainWindowUi.actionUnderline.setChecked(mainWindowUi.textNote.fontUnderline());
	   mainWindowUi.actionStrikethrough.setChecked(mainWindowUi.textNote.currentCharFormat().fontStrikeOut());
	
	   // current font
	   mainWindowUi.comboFonts.setCurrentIndex(mainWindowUi.comboFonts.findText(mainWindowUi.textNote.currentFont().family()));
	   mainWindowUi.comboFontSizes.setCurrentIndex(mainWindowUi.comboFontSizes.findText(Integer.toString(mainWindowUi.textNote.currentFont().pointSize())));
	   String name = mainWindowUi.textNote.currentCharFormat().foreground().color().name();
	   if(name.equals("#000000")) // Qt::black
		   mainWindowUi.comboFontColors.setCurrentIndex(1);
	   else if(name.equals("#ff0000")) // Qt::red
		   mainWindowUi.comboFontColors.setCurrentIndex(2);
	   else if(name.equals("#0000ff")) // Qt::blue
		   mainWindowUi.comboFontColors.setCurrentIndex(3);
	   else if(name.equals("#008000")) // Qt::darkGreen
		   mainWindowUi.comboFontColors.setCurrentIndex(4);
	   else if(name.equals("#a0a0a4")) // Qt::gray
		   mainWindowUi.comboFontColors.setCurrentIndex(5);
	   else // all other colors
			mainWindowUi.comboFontColors.setCurrentIndex(0);
	
		// current alignment
		mainWindowUi.actionLeft.setChecked(mainWindowUi.textNote.alignment().value() == Qt.AlignmentFlag.AlignLeft.value());
		mainWindowUi.actionCenter.setChecked(mainWindowUi.textNote.alignment().value() == Qt.AlignmentFlag.AlignCenter.value());
		mainWindowUi.actionRight.setChecked(mainWindowUi.textNote.alignment().value() == Qt.AlignmentFlag.AlignRight.value());
		mainWindowUi.actionFull.setChecked(mainWindowUi.textNote.alignment().value() == Qt.AlignmentFlag.AlignJustify.value());
	
		// current list style
		if(mainWindowUi.textNote.textCursor().currentList() != null) {
			mainWindowUi.actionBullet_List.setChecked(mainWindowUi.textNote.textCursor().currentList().textListFormat().style() == QTextListFormat.Style.ListDisc);
			mainWindowUi.actionNumeric_List.setChecked(mainWindowUi.textNote.textCursor().currentList().textListFormat().style() == QTextListFormat.Style.ListDecimal);
		} else {
			mainWindowUi.actionBullet_List.setChecked(false);
			mainWindowUi.actionNumeric_List.setChecked(false);
		}
	}
   
   @Override
   public boolean eventFilter(QObject object, QEvent event) {
   	if (event.type() == QEvent.Type.FocusOut)
   	{
   		if (object == mainWindowUi.textNote)
   		{
   			saveNoteText();
   		}
   	}
   	else if (event.type() == QEvent.Type.KeyPress)
   	{
   		if(object == mainWindowUi.textNote)
   		{
   			QKeyEvent keyEvent = (QKeyEvent)event;
   			// trap tab key
   			if (keyEvent.key() == Qt.Key.Key_Tab.value()) {
   				// increase indent in lists
   				if (mainWindowUi.textNote.textCursor().currentList() != null)
   					increaseIndent();
   				// add two spaces in non-lists
   				else {
   					mainWindowUi.textNote.insertPlainText("\t");
   				}
   				return true;
   			}
   			// trap backtab key
   			else if(keyEvent.key() == Qt.Key.Key_Backtab.value())
   			{
   				// decrease indent if list
   				if (mainWindowUi.textNote.textCursor().currentList() != null) {
   					decreaseIndent();
   				}
   				return true;
   			}
   		}
   	}
   	return false;
   }
}
