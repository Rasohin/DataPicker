package ru.slybeaver.datapicker;


import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by slybeaver on 27.03.2015.
 */
public class DataPicker extends View {
    public Context dataPickercontext=null;
    private OnChangeValueListener mListener=null;


    public int nowTopPosition = 0; //Позиция скрола
    private int upMaxTopPosition = 0; //Максимальная позиция, в которую может уехать скрол
    private int maxTopPosition = 0; //Максимальная позиция скрола
    private int maxValueHeight = 0; //Высота значения
    private ArrayList<dpValuesSize> dpvalues = new ArrayList<dpValuesSize>(); //Значения
    private int canvasW =0; //Текущая ширина холста
    private int canvasH=0; //Текущая высота холста

    private int selectedvalueId=0;
    private boolean needAnimation=false; //Включать ли анимацию перемещения
    private int needPosition=0; // Нуобходимая позиция

    public int valpadding = 30; //Отступ мужду значениями

    private int scrollspeed=0; //Импульсная скорость скрола
    private boolean scrolltoup=false; //Движется ли скрол вверх

    private  float dpDownY=0; //Координаты клика по канвасу с учетом смещения
    private float canvasDownY=0; //Координаты точного клика по канвасу

    private long act_downTime=0; //Момент времени в который был совершен тап по холсту



    public interface OnChangeValueListener{
        public void onEvent(int valueId);
    }

    public void setOnChangeValueListener(OnChangeValueListener eventListener) {
        mListener=eventListener;
    }


    public DataPicker(Context context) {
        super(context);
        dataPickercontext=context;
        init();

    }


    public DataPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        dataPickercontext=context;
        init();
    }

    public DataPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        dataPickercontext=context;
        init();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        canvasW = w ;
        canvasH = h ;
        maxValueHeight = (canvasH - (valpadding*2))/2;
        nowTopPosition = 0;
        super.onSizeChanged(w, h, oldw, oldh);
    }



    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
            canvasDownY = motionEvent.getY();
            dpDownY=  motionEvent.getY() - nowTopPosition;
            needAnimation=false;
            act_downTime=motionEvent.getEventTime();

        }
        if (motionEvent.getAction()==MotionEvent.ACTION_MOVE){
            if ((int) (motionEvent.getY()-dpDownY)>maxTopPosition){nowTopPosition=maxTopPosition; return true;}
            if ((int) (motionEvent.getY()-dpDownY)<upMaxTopPosition){nowTopPosition=upMaxTopPosition;return true;}
            nowTopPosition= (int) (motionEvent.getY()-dpDownY);
        }

        if (motionEvent.getAction()==MotionEvent.ACTION_UP){
            if (canvasDownY>motionEvent.getY() ){scrolltoup=false;}else {scrolltoup=true;}

            if ((motionEvent.getEventTime()-act_downTime<200)&&(Math.abs(dpDownY-motionEvent.getY())>100)){
                scrollspeed= (int) ( 1000-(motionEvent.getEventTime()-act_downTime));

            } else {
                scrollspeed=0;
                roundingValue();
            }
            needAnimation=true;

        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if (dpvalues.size() == 0) {
                return;
            }

            upMaxTopPosition = -(((dpvalues.size() - 1) * (maxValueHeight + valpadding)));
            canvas.drawColor(Color.argb(0, 255, 255, 255));
            //canvas.drawColor(dataPickercontext.getResources().getColor(R.color.datepickerBackground));

            if (needAnimation) {

                if (scrollspeed > 0) {
                    scrollspeed -= 30;

                    if (scrolltoup) {


                        int currentPos = nowTopPosition + 30;
                        if ((currentPos) > maxTopPosition) {
                            nowTopPosition = maxTopPosition;
                            scrollspeed = 0;
                            roundingValue();
                        } else {
                            nowTopPosition = currentPos;
                        }
                    }

                    if (!scrolltoup) {
                        int currentPos = nowTopPosition - 30;
                        if ((currentPos) < upMaxTopPosition) {
                            nowTopPosition = upMaxTopPosition;
                            scrollspeed = 0;
                            roundingValue();
                        } else {
                            nowTopPosition = currentPos;
                        }
                    }

                    if (scrollspeed <= 0) {
                        roundingValue();
                    }


                } else {


                    if (nowTopPosition > needPosition) {
                        nowTopPosition -= 20;
                        if (nowTopPosition < needPosition) {
                            nowTopPosition = needPosition;
                        }
                    }

                    if (nowTopPosition < needPosition) {
                        nowTopPosition += 20;
                        if (nowTopPosition > needPosition) {
                            nowTopPosition = needPosition;
                        }
                    }

                    if (nowTopPosition == needPosition) {
                        needAnimation = false;
                    }
                }
            }


            //Вставляем значения
            for (int i = 0; i < dpvalues.size(); i++) {
                try {
                    Paint paint = new Paint();
                    paint.setColor(dataPickercontext.getResources().getColor(R.color.datepickerText));
                    if (selectedvalueId == i) {
                        paint.setColor(dataPickercontext.getResources().getColor(R.color.datepickerSelectedValue));
                        Paint shadowText = new Paint();
                        shadowText.setColor(dataPickercontext.getResources().getColor(R.color.datepickerSelectedValueShadow));
                        shadowText.setTextSize(dpvalues.get(i).dpTextSize);

                        shadowText.setAntiAlias(true);
                        canvas.drawText(dpvalues.get(i).dpValue, (canvasW / 2) - (dpvalues.get(i).dpWidth / 2), ((maxValueHeight + valpadding) * i) + (valpadding + maxValueHeight) + (dpvalues.get(i).dpHeight / 2) + nowTopPosition + 2, shadowText);
                    }
                    paint.setTextSize(dpvalues.get(i).dpTextSize);
                    paint.setAntiAlias(true);

                    canvas.drawText(dpvalues.get(i).dpValue, (canvasW / 2) - (dpvalues.get(i).dpWidth / 2), ((maxValueHeight + valpadding) * i) + (valpadding + maxValueHeight) + (dpvalues.get(i).dpHeight / 2) + nowTopPosition, paint);
                } catch (Exception e) {
                }
            }


            //Рисуем боковые границы виджета
            Paint lPBorders = new Paint();
            lPBorders.setColor(dataPickercontext.getResources().getColor(R.color.datapickerBlackLines));
            canvas.drawLine(0, 0, 0, canvasH, lPBorders);
            canvas.drawLine(1, 0, 1, canvasH, lPBorders);
            canvas.drawLine(canvasW - 1, 0, canvasW - 1, canvasH, lPBorders);
            canvas.drawLine(canvasW - 2, 0, canvasW - 2, canvasH, lPBorders);
            canvas.drawLine(canvasW, 0, canvasW, canvasH, lPBorders);

            lPBorders = new Paint();
            lPBorders.setColor(dataPickercontext.getResources().getColor(R.color.datapickerGrayLines));

            canvas.drawRect(2, 0, 7, canvasH, lPBorders);

            canvas.drawRect(canvasW - 7, 0, canvasW - 2, canvasH, lPBorders);


            //Рисуем затенения
            Paint framePaint = new Paint();
            framePaint.setShader(new LinearGradient(0, 0, 0, getHeight() / 5, dataPickercontext.getResources().getColor(R.color.datapickerGradientStart), Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawPaint(framePaint);
            framePaint.setShader(new LinearGradient(0, getHeight(), 0, getHeight() - getHeight() / 5, dataPickercontext.getResources().getColor(R.color.datapickerGradientStart), Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawPaint(framePaint);

            //Рисуем полоску веделенного текста
            Path pathSelect = new Path();
            pathSelect.moveTo(0, canvasH / 2 - maxValueHeight / 2 - valpadding / 2);
            pathSelect.lineTo(canvasW, canvasH / 2 - maxValueHeight / 2 - valpadding / 2);
            pathSelect.lineTo(canvasW, canvasH / 2);
            pathSelect.lineTo(0, canvasH / 2);
            pathSelect.lineTo(0, canvasH / 2 - maxValueHeight / 2);
            Paint pathSelectPaint = new Paint();
            pathSelectPaint.setShader(new LinearGradient(0, 0, 0, maxValueHeight / 2, dataPickercontext.getResources().getColor(R.color.datapickerSelectedValueeLineG1), dataPickercontext.getResources().getColor(R.color.datapickerSelectedValueeLineG2), Shader.TileMode.CLAMP));
            canvas.drawPath(pathSelect, pathSelectPaint);

            pathSelect = new Path();
            pathSelect.moveTo(0, canvasH / 2);
            pathSelect.lineTo(canvasW, canvasH / 2);
            pathSelect.lineTo(canvasW, canvasH / 2 + maxValueHeight / 2 + valpadding / 2);
            pathSelect.lineTo(0, canvasH / 2 + maxValueHeight / 2 + valpadding / 2);
            pathSelect.lineTo(0, canvasH / 2);
            pathSelectPaint = new Paint();
            pathSelectPaint.setShader(new LinearGradient(0, 0, 0, maxValueHeight / 2, dataPickercontext.getResources().getColor(R.color.datapickerSelectedValueeLineG3), dataPickercontext.getResources().getColor(R.color.datapickerSelectedValueeLineG4), Shader.TileMode.CLAMP));
            canvas.drawPath(pathSelect, pathSelectPaint);


            //Рисуем рамку выделенного значения
            Paint selValLightBorder = new Paint();
            Paint selValTopBorder = new Paint();
            Paint selValBottomBorder = new Paint();
            selValLightBorder.setColor(dataPickercontext.getResources().getColor(R.color.datapicketSelectedValueBorder));
            selValTopBorder.setColor(dataPickercontext.getResources().getColor(R.color.datapicketSelectedBorderTop));
            selValBottomBorder.setColor(dataPickercontext.getResources().getColor(R.color.datapicketSelectedBorderBttom));
            canvas.drawLine(0, canvasH / 2 - maxValueHeight / 2 - valpadding / 2, canvasW, canvasH / 2 - maxValueHeight / 2 - valpadding / 2, selValLightBorder);
            canvas.drawLine(0, canvasH / 2 - maxValueHeight / 2 - valpadding / 2 + 1, canvasW, canvasH / 2 - maxValueHeight / 2 - valpadding / 2 + 1, selValTopBorder);
            canvas.drawLine(0, canvasH / 2 + maxValueHeight / 2 + valpadding / 2, canvasW, canvasH / 2 + maxValueHeight / 2 + valpadding / 2, selValLightBorder);
            canvas.drawLine(0, canvasH / 2 + maxValueHeight / 2 + valpadding / 2 - 1, canvasW, canvasH / 2 + maxValueHeight / 2 + valpadding / 2 - 1, selValBottomBorder);

            canvas.drawLine(0, canvasH / 2 - maxValueHeight / 2 - valpadding / 2, 0, canvasH / 2 + maxValueHeight / 2 + valpadding / 2, selValLightBorder);
            canvas.drawLine(1, canvasH / 2 - maxValueHeight / 2 - valpadding / 2, 1, canvasH / 2 + maxValueHeight / 2 + valpadding / 2, selValLightBorder);
            canvas.drawLine(canvasW - 1, canvasH / 2 - maxValueHeight / 2 - valpadding / 2, canvasW - 1, canvasH / 2 + maxValueHeight / 2 + valpadding / 2, selValLightBorder);
            canvas.drawLine(canvasW - 2, canvasH / 2 - maxValueHeight / 2 - valpadding / 2, canvasW - 2, canvasH / 2 + maxValueHeight / 2 + valpadding / 2, selValLightBorder);
            canvas.drawLine(canvasW, canvasH / 2 - maxValueHeight / 2 - valpadding / 2, canvasW, canvasH / 2 + maxValueHeight / 2 + valpadding / 2, selValLightBorder);


            //Рисуем выделенный текст
            Paint selectedTextPaint = new Paint();
            selectedTextPaint.setColor(dataPickercontext.getResources().getColor(R.color.datepickerSelectedValue));
            Paint shadowText = new Paint();
            shadowText.setColor(dataPickercontext.getResources().getColor(R.color.datepickerSelectedValueShadow));
            shadowText.setTextSize(dpvalues.get(selectedvalueId).dpTextSize);
            shadowText.setAntiAlias(true);
            canvas.drawText(dpvalues.get(selectedvalueId).dpValue, (canvasW / 2) - (dpvalues.get(selectedvalueId).dpWidth / 2), ((maxValueHeight + valpadding) * selectedvalueId) + (valpadding + maxValueHeight) + (dpvalues.get(selectedvalueId).dpHeight / 2) + nowTopPosition + 2, shadowText);
            selectedTextPaint.setTextSize(dpvalues.get(selectedvalueId).dpTextSize);
            selectedTextPaint.setAntiAlias(true);
            canvas.drawText(dpvalues.get(selectedvalueId).dpValue, (canvasW / 2) - (dpvalues.get(selectedvalueId).dpWidth / 2), ((maxValueHeight + valpadding) * selectedvalueId) + (valpadding + maxValueHeight) + (dpvalues.get(selectedvalueId).dpHeight / 2) + nowTopPosition, selectedTextPaint);

        }catch(Exception e){}

        //Перерисовка канваса
        this.postInvalidateDelayed( 1000 / 80);

    }

    private void init(){

    }



    //Выравнивание значения
    private void roundingValue(){
        Log.d("Rounding", "ROUNDING VALUE!");
        //selectedvalueId=Math.abs(((nowTopPosition-valpadding-(maxValueHeight/2))/(maxValueHeight+valpadding)));

        needPosition = (((nowTopPosition-maxTopPosition-(maxValueHeight/2))/(maxValueHeight+valpadding))) * (maxValueHeight + valpadding) + maxTopPosition ;
        selectedvalueId=Math.abs(((needPosition-valpadding-(maxValueHeight/2))/(maxValueHeight+valpadding)));
        try {Log.e("SELECTED VALUE", dpvalues.get(selectedvalueId).dpValue);} catch (Exception e){}

        onSelected(selectedvalueId);

    }

    //Возвращаем выбранное значение
    public String getValue(){
        try {return  dpvalues.get(selectedvalueId).dpValue;} catch (Exception e){}
        return null;
    }

    //Устанавливаем в нужное значение
    private Handler dpChangeHandler = new Handler();
    public void changetToValue(final int valueId){
        selectedvalueId=valueId;

            dpChangeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (canvasW==0||canvasH==0){
                        dpChangeHandler.postDelayed(this,100);
                    } else {
                        maxValueHeight = (canvasH - (valpadding*2))/2;
                        needPosition = -((maxValueHeight + valpadding) * (valueId));
                        needAnimation=true;
                    }
                }
            },100);


        onSelected(selectedvalueId);

    }


    //Событие, когда было изменено значение
    protected void onSelected(int selectedId){

        if (mListener!=null){mListener.onEvent(selectedId);}
    }


    //Возвращаем идентификатор выбранного значения
    public int getValueid(){
        try {return  selectedvalueId;} catch (Exception e){}
        return -1;
    }


    private Handler dpHandler = new Handler();
    public void setValues(final String[] newvalues){
        if (canvasW==0||canvasH==0){
            dpHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (canvasW==0||canvasH==0){
                        dpHandler.postDelayed(this,100);
                    } else {
                        dpvalues.clear();
                        for (int i=0; i<newvalues.length;i++){
                            dpvalues.add(new dpValuesSize(newvalues[i], canvasW, canvasH));
                        }
                    }
                }
            },100);
        }
        dpvalues.clear();
        for (int i=0; i<newvalues.length;i++){
            dpvalues.add(new dpValuesSize(newvalues[i], canvasW, canvasH));
        }

    }



}





class dpValuesSize {
    public int dpWidth=0;
    public int dpHeight=0;
    public String dpValue ="";
    public int dpTextSize=0;
    public int valpadding = 30; //Отступ мужду значениями
    public int valinnerLeftpadding=20; //Отступ по краям у значения


    public dpValuesSize(String val, int canvasW, int canvasH){
        try {
            int maxTextHeight = (canvasH - (valpadding * 2)) / 2;
            boolean sizeOK = false;
            dpValue = val;
            while (!sizeOK) {
                Rect textBounds = new Rect();
                Paint textPaint = new Paint();
                dpTextSize++;
                textPaint.setTextSize(dpTextSize);

                textPaint.getTextBounds(val, 0, val.length(), textBounds);
                if (textBounds.width() <= canvasW - (valinnerLeftpadding * 2) && textBounds.height() <= maxTextHeight) {
                    dpWidth = textBounds.width();
                    dpHeight = textBounds.height();
                } else {
                    sizeOK = true;
                }

            }
        } catch (Exception e){}
    }
}
