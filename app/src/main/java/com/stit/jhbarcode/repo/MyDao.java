package com.stit.jhbarcode.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.stit.jhbarcode.model.CodMast;
import com.stit.jhbarcode.model.CodMastKind;
import com.stit.jhbarcode.model.Kind;
import com.stit.jhbarcode.model.MainData;

public class MyDao {
  private String sql;
  SQLiteDatabase db;

  private final SimpleDateFormat dateFormatWithSlash = new SimpleDateFormat("yyyy/MM/dd");
  private final SimpleDateFormat dateFormatNoSlash = new SimpleDateFormat("yyyyMMdd");

  public MyDao(Context context){
    this.db = DbHelper.getInstance(context).getDb();
  }

  /** cod_mast, login 成功後, Oracle db to SQLite db */
  public boolean insertCodMast(Collection<CodMast> codMastList) {
    try {
      db.beginTransaction();

      for (CodMast codMast : codMastList) {
        ContentValues cv = new ContentValues();
        cv.put("kind", codMast.getKind());
        cv.put("code_no", codMast.getCodeNo());
        cv.put("code_name", codMast.getCodeName());

        db.insert("cod_mast", null, cv);
      }

      // commit
      db.setTransactionSuccessful();

    } catch (Exception ex) {
      return false;
    } finally {
      db.endTransaction();  // rollback if not commit
    }

    return true;
  }


  public boolean deleteCodMast() {
    try {
      sql = "delete from cod_mast";
      db.execSQL(sql);

    } catch (Exception ex) {
      return false;
    } finally {
    }

    return true;
  }

  // 2020
  public List<MainData> getMainDataList(Kind kind) {
    List<MainData> list = new ArrayList<>();

    switch (kind) {
      case JobOrderPickup:
        sql = "select * from main_data where kind = ? order by scan_date, bar_code";
        break;

      case LocationMove:
        sql = "select * from main_data where kind = ? order by scan_date, bar_code";
        break;

      case MatReturn:
        sql = "select * from main_data where kind = ? order by scan_date, bar_code";
        break;

      case PicklingFinished:
        sql = "select * from main_data where kind = ? order by scan_date, bar_code";
        break;

      case PlatingReception:
        sql = "select * from main_data where kind = ? order by scan_date, locate";
        break;
    }

    String[] params = new String[] {
       kind.getValue(),
    };

    Cursor cursor = db.rawQuery(sql, params);

    while (cursor.moveToNext()) {
      MainData md = new MainData();

      md.setId(cursor.getLong(cursor.getColumnIndex("_id")));
      md.setProcEmp(cursor.getString(cursor.getColumnIndex("proc_emp")));
      md.setKind(cursor.getString(cursor.getColumnIndex("kind")));
      md.setBarCode(cursor.getString(cursor.getColumnIndex("bar_code")));
      md.setLocate(cursor.getString(cursor.getColumnIndex("locate")));
      md.setScwJobNo(cursor.getString(cursor.getColumnIndex("scw_job_no")));
      md.setItemNo(cursor.getInt(cursor.getColumnIndex("item_no")));
      md.setIsrtType(cursor.getString(cursor.getColumnIndex("isrt_type")));
      md.setReasonCode(cursor.getString(cursor.getColumnIndex("reason_code")));
      md.setClassNo(cursor.getString(cursor.getColumnIndex("class_no")));
      md.setPassYn(cursor.getString(cursor.getColumnIndex("pass_yn")));
      md.setScanDate(cursor.getString(cursor.getColumnIndex("scan_date")));

      list.add(md);
    }

    cursor.close();

    return list;
  }

  /** kind, scanDate */
  public List<MainData> getMainDataList(Kind kind, Date scanDate) {
    List<MainData> list = new ArrayList<>();

    String yyyymmdd = this.dateFormatNoSlash.format(scanDate);

    switch (kind) {
      case JobOrderPickup:
        sql = "select * from main_data where kind = ? and substr(scan_date, 1, 8) = ? order by scan_date, bar_code";
        break;

      case LocationMove:
        sql = "select * from main_data where kind = ? and substr(scan_date, 1, 8) = ? order by scan_date, bar_code";
        break;

      case MatReturn:
        sql = "select * from main_data where kind = ? and substr(scan_date, 1, 8) = ? order by scan_date, bar_code";
        break;

      case PicklingFinished:
        sql = "select * from main_data where kind = ? and substr(scan_date, 1, 8) = ? order by scan_date, bar_code";
        break;

      case PlatingReception:
        sql = "select * from main_data where kind = ? and substr(scan_date, 1, 8) = ? order by scan_date, locate";
        break;
    }

    String[] params = new String[] {
            kind.getValue(),
            yyyymmdd
    };

    Cursor cursor = db.rawQuery(sql, params);

    while (cursor.moveToNext()) {
      MainData md = new MainData();

      md.setId(cursor.getLong(cursor.getColumnIndex("_id")));
      md.setProcEmp(cursor.getString(cursor.getColumnIndex("proc_emp")));
      md.setKind(cursor.getString(cursor.getColumnIndex("kind")));
      md.setBarCode(cursor.getString(cursor.getColumnIndex("bar_code")));
      md.setLocate(cursor.getString(cursor.getColumnIndex("locate")));
      md.setScwJobNo(cursor.getString(cursor.getColumnIndex("scw_job_no")));
      md.setItemNo(cursor.getInt(cursor.getColumnIndex("item_no")));
      md.setIsrtType(cursor.getString(cursor.getColumnIndex("isrt_type")));
      md.setReasonCode(cursor.getString(cursor.getColumnIndex("reason_code")));
      md.setClassNo(cursor.getString(cursor.getColumnIndex("class_no")));
      md.setPassYn(cursor.getString(cursor.getColumnIndex("pass_yn")));
      md.setScanDate(cursor.getString(cursor.getColumnIndex("scan_date")));

      list.add(md);
    }

    cursor.close();

    return list;
  }

  // 線材退料 reason code, name
  public List<CodMast> getCodMastList(CodMastKind kind) {
    List<CodMast> list = new ArrayList<>();

    sql = "select * from cod_mast where kind = ? order by code_no desc";
    String[] params = {
        kind.getValue()
    };

    Cursor cursor = db.rawQuery(sql, params);

    while (cursor.moveToNext()) {
      CodMast codMast = new CodMast();
      codMast.setKind(cursor.getString(0));
      codMast.setCodeNo(cursor.getString(1));
      codMast.setCodeName(cursor.getString(2));
      list.add(codMast);
    }
    cursor.close();

    return list;
  }

  public boolean isExistMainData(Kind kind) {
    String sql = "select count(*) from main_data where " +
            " kind = ?";

    String[] params = new String[] {
            kind.getValue()
    };

    Cursor cursor = db.rawQuery(sql, params);
    cursor.moveToFirst();
    int count = cursor.getInt(0);

    cursor.close();

    return (count > 0) ? true : false;
  }

  public boolean isExistMainData(Kind kind, String barcode) {
    String sql = "select count(*) from main_data where " +
            " kind = ? and " +
            " bar_code = ?";

    String[] params = new String[] {
        kind.getValue(),
        barcode
    };

    Cursor cursor = db.rawQuery(sql, params);
    cursor.moveToFirst();
    int count = cursor.getInt(0);

    cursor.close();

    return (count > 0) ? true : false;
  }

  public boolean isExistMainData(Kind kind, String locate, String jobNo) {
    String sql = "select count(*) from main_data where " +
            " kind = ? and " +
            " locate = ? and " +
            " scw_job_no = ?";

    String[] params = new String[] {
            kind.getValue(),
            locate,
            jobNo
    };

    Cursor cursor = db.rawQuery(sql, params);
    cursor.moveToFirst();
    int count = cursor.getInt(0);

    cursor.close();

    return (count > 0) ? true : false;
  }



  public boolean insertMainData(MainData mainData) {
    ContentValues cv = new ContentValues();

    //System.out.println(mainData.getClassNo() + "-hello");

    cv.put("proc_emp", mainData.getProcEmp());
    cv.put("kind", mainData.getKind());
    cv.put("bar_code", mainData.getBarCode());
    cv.put("locate", mainData.getLocate());
    cv.put("scw_job_no", mainData.getScwJobNo());
    cv.put("item_no", mainData.getItemNo());
    cv.put("isrt_type", mainData.getIsrtType());
    cv.put("reason_code", mainData.getReasonCode());
    cv.put("class_no", mainData.getClassNo());
    cv.put("pass_yn", mainData.getPassYn());
    cv.put("scan_date", mainData.getScanDate());

    // -1 if fail
    long rowId = this.db.insert("main_data", null, cv);
    //System.out.println("rowId:"  + rowId);

    return (rowId != -1) ? true : false;
  }

  public boolean deleteMainData(Kind kind) {
    try {
      sql = "delete from main_data where kind = ?";
      String[] params = {
         kind.getValue()
      };

      db.execSQL(sql, params);

    } catch (Exception ex) {
      return false;
    }

    return true;
  }

  /** 工令領用細項刪除 */
  public boolean deleteMainDataById(long id) {
    try {
      String where = "_id = ?";

      String[] params = {
         String.valueOf(id)
      };

      int count = db.delete("main_data", where, params);

      if (count != 1) {
        return false;
      }

    } catch (Exception ex) {
        return false;
    }

    return true;
  }


  /**
   * 線材移轉細項刪除
   * using delete because the affect row count
   *
   */
  public boolean deleteLocationMove(String barcode) {
    try {
      //sql = "delete from main_data where kind = ? and bar_code = ?";
      //String[] params = {
      //        Kind.LocationMove.getValue(),
      //        barcode
      //};
      //db.execSQL(sql, params);

      String[] params = {
              Kind.LocationMove.getValue(),
              barcode
      };

      int count = db.delete("main_data", "kind = ? and bar_code = ?", params);
      if (count != 1) {
        return false;
      }

    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      return false;
    } finally {
    }

    return true;
  }



} // end class
