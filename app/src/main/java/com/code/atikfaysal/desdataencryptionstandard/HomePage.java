package com.code.atikfaysal.desdataencryptionstandard;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity
{

    private static final String TAG = "Content values";
    private TextView txtCipherText,txtError1,txtError2;
    private EditText txtPlainText,txtKey;

    private String cipherText;


    //s-box 4/16 matrix
    private String[][] sBox = new String[][]{
            {"63","7C","77","7B","F2","6B","6F","C5" ,"30","01","67","2B","FE","D7","AB","76"},
            {"CA","82","C9","7D","FA","59","47","F0" ,"AD","D4","A2","AF","9C","A4","72","C0"},
            {"B7","FD","93","26","36","3F","F7","CC" ,"34","A5","E5","F1","71","D8","31","15"},
            {"04","C7","23","C3","18","96","05","9A" ,"07","12","80","E2","EB","27","B2","75"}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initComponent();
        checkText();
    }


    //initialize all component
    private void initComponent()
    {
        txtCipherText = findViewById(R.id.cipherText);
        txtError1 = findViewById(R.id.text1);
        txtError2 = findViewById(R.id.text2);
        txtPlainText = findViewById(R.id.plainText);
        txtKey = findViewById(R.id.key);
        Button bEncrypt = findViewById(R.id.encrypt);


        //on button click ,encrypt button;
        bEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtError1.getText().toString().equals("OK")&&txtError2.getText().toString().equals("OK"))
                {
                    String permutedText,permutedKey;
                    permutedText = initialPermutation(txtPlainText.getText().toString());
                    permutedKey = initialPermutation(txtKey.getText().toString());

                    int i=1;
                    String leftText,rightText,leftKey,rightKey;
                    String plainText[] = dividedTextAndKey(permutedText);
                    leftText = plainText[0];
                    rightText = plainText[1];

                    String keyValue[] = dividedTextAndKey(permutedKey.substring(1,15));
                    leftKey = keyValue[0];
                    rightKey = keyValue[1];
                    while(i<=16)
                    {
                        Log.d(TAG,"loop : "+i);
                        Log.d(TAG,"left text : "+leftText);
                        Log.d(TAG,"right text : "+rightText);
                        List<String>values = plainTextToCipherText(leftText,rightText,leftKey,rightKey);
                        leftText = values.get(0);
                        rightText = values.get(1);

                        if(i==16)
                            cipherText = leftText+rightText;

                        i++;
                    }

                    txtCipherText.setText(cipherText);
                }
                else
                    Toast.makeText(HomePage.this,"Please input valid text and valid key.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //here check data ,if plain text or key is not equal to 16 length or is not hexa then it will get an error
    private void checkText()
    {
        txtPlainText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean flag = true;
                String text = txtPlainText.getText().toString();
                if(txtPlainText.length()!=16)
                    flag = false;

                for(int i=0;i<txtPlainText.length();i++)
                {
                    if((text.charAt(i)>='A'&&text.charAt(i)<='F')||(text.charAt(i)>='0'&&text.charAt(i)<='9'))
                    {

                    }else flag = false;
                }

                if(!flag)
                {
                    txtError1.setText("Invalid");
                    txtError1.setTextColor(Color.parseColor("#FF1744"));

                }
                else
                {
                    txtError1.setText("OK");
                    txtError1.setTextColor(Color.parseColor("#00E676"));
                }
            }
        });


        txtKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean flag = true;
                String tKey = txtKey.getText().toString();
                if(tKey.length()!=16)
                    flag = false;

                for(int i=0;i<tKey.length();i++)
                {
                    if((tKey.charAt(i)>='A'&&tKey.charAt(i)<='F')||(tKey.charAt(i)>='0'&&tKey.charAt(i)<='9'))
                    {

                    }else flag = false;
                }

                if(!flag)
                {
                    txtError2.setText("Invalid");
                    txtError2.setTextColor(Color.parseColor("#FF1744"));

                }
                else
                {
                    txtError2.setText("OK");
                    txtError2.setTextColor(Color.parseColor("#00E676"));
                }
            }
        });
    }

    //first permutation
    private String initialPermutation(String text)
    {
        StringBuilder permutedText= new StringBuilder();

        int length = text.length()-1;//cause array index is (0-15)

        try {
            for(int i=length;i>=0;i--)
            {
                if(i%2==0)//first concat with even index character
                    permutedText.append(text.charAt(i));
            }
            for(int i=length;i>=0;i--)
            {
                if(i%2!=0)//then concat with odd index character
                    permutedText.append(text.charAt(i));
            }
        }catch (StringIndexOutOfBoundsException e)
        {
            Toast.makeText(HomePage.this,"Error : "+e.toString(),Toast.LENGTH_SHORT).show();//if any error then show here
        }

        return permutedText.toString();//return permuted string
    }

    //converting here plain text to cipher text
    private List<String> plainTextToCipherText(String leftText,String rightText,String leftKey,String rightKey)
    {
        List<String> values = new ArrayList<>();
        String expPermutedString = initialPermutation(rightText+leftText.substring(3,7));//expand 32 to 48 bit
        String expPermutedKey = leftShiftAndPermutation(leftKey,rightKey);//expand 56 to 48 and left shift

        List<String>binary = xorString(expPermutedString,expPermutedKey,6);//get xor value 48 bit string

        StringBuilder builder = new StringBuilder();
        for(int i=0;i<binary.size();i++)
            builder.append(sBoxValue(binary.get(i)));//get value from sbox

        leftText = rightText;//swap right to left
        rightText = convertTo32Bit(builder.toString());//xorLeftAndRight(convertTo32Bit(builder.toString()),leftText);
        rightText = xorLeftAndRight(leftText,rightText);//last xor value

        values.add(leftText);
        values.add(rightText);
        values.add(expPermutedKey);

        return values;
    }

    //split text and key
    private String[] dividedTextAndKey(String value)
    {
        String text[] = {"",""};
        for(int i=0;i<value.length();i++)
        {
            if(i<value.length()/2)text[0]+=value.charAt(i);
            else text[1]+=value.charAt(i);
        }


        return text;
    }

    //left shift and permutation
    private String leftShiftAndPermutation(String lKey, String rKey)
    {
        StringBuilder leftKey = new StringBuilder();
        StringBuilder rightKey = new StringBuilder();

        try {
            for(int i=1;i<lKey.length();i++) {
                leftKey.append(lKey.charAt(i));
            }

            leftKey.append(lKey.charAt(0));

            for(int i=1;i<rKey.length();i++) {
                rightKey.append(rKey.charAt(i));
            }

            rightKey.append(rKey.charAt(0));
            String key = leftKey.append(rightKey).toString();

            return initialPermutation(key.substring(1,13));
        }catch (ArrayIndexOutOfBoundsException e)
        {
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    //xor here
    private List<String> xorString(String text,String key,int div)
    {
        List<String>binaryText;
        List<String>binaryKey;

        binaryText = toBinary(text);
        binaryKey = toBinary(key);

        List<String>binaryString = new ArrayList<>();

        int i=0,j=0;
        while (i<binaryText.size()&&j<binaryKey.size())
        {
            if(binaryText.get(i).equals(binaryKey.get(j)))
                binaryString.add("0");//if equal then store 0 else store 1
            else binaryString.add("1");

            i++;
            j++;
        }

        List<String>binary = new ArrayList<>();//store binary in 6 or 4
        i=0;j=0;int count,index=0;
        while(i<binaryString.size()/div)
        {
            count =0;
            StringBuilder str = new StringBuilder();
            for(j=index;j<binaryString.size();j++)
            {
                if(count==div)
                {
                    index=j;
                    break;
                }
                str.append(binaryString.get(j));
                count++;
            }
            binary.add(str.toString());
            i++;
        }
        return binary;
    }

    //last xor left text and right text
    private String xorLeftAndRight(String l,String r)
    {
        StringBuilder convertedValue = new StringBuilder();
        List<String>binary = xorString(l,r,4);
        for (int i=0;i<binary.size();i++)
            convertedValue.append(convertBinaryToHex(binary.get(i)));

        return convertedValue.toString();
    }

    //hexa to binary
    private List<String> toBinary(String value)
    {
        List<String> binary = new ArrayList<>();
        int i=0;
        while(i<value.length())
        {
            switch (value.charAt(i))
            {
                case '0':
                    binary.add("0");binary.add("0");binary.add("0");binary.add("0");
                    break;
                case '1':
                    binary.add("0");binary.add("0");binary.add("0");binary.add("1");
                    break;
                case '2':
                    binary.add("0");binary.add("0");binary.add("1");binary.add("0");
                    break;
                case '3':
                    binary.add("0");binary.add("0");binary.add("1");binary.add("1");
                    break;
                case '4':
                    binary.add("0");binary.add("1");binary.add("0");binary.add("0");
                    break;
                case '5':
                    binary.add("0");binary.add("1");binary.add("0");binary.add("1");
                    break;
                case '6':
                    binary.add("0");binary.add("1");binary.add("1");binary.add("0");
                    break;
                case '7':
                    binary.add("0");binary.add("1");binary.add("1");binary.add("1");
                    break;
                case '8':
                    binary.add("1");binary.add("0");binary.add("0");binary.add("0");
                    break;
                case '9':
                    binary.add("1");binary.add("0");binary.add("0");binary.add("1");
                    break;
                case 'A':
                    binary.add("1");binary.add("0");binary.add("1");binary.add("0");
                    break;
                case 'B':
                    binary.add("1");binary.add("0");binary.add("1");binary.add("1");
                    break;
                case 'C':
                    binary.add("1");binary.add("1");binary.add("0");binary.add("0");
                    break;
                case 'D':
                    binary.add("1");binary.add("1");binary.add("0");binary.add("1");
                    break;
                case 'E':
                    binary.add("1");binary.add("1");binary.add("1");binary.add("0");
                    break;
                case 'F':
                    binary.add("1");binary.add("1");binary.add("1");binary.add("1");
                    break;

            }
            i++;
        }
        return binary;
    }

    //get sbox value
    private String sBoxValue(String binaryString)
    {
        try {
            String index = Character.toString(binaryString.charAt(0)).concat(Character.toString(binaryString.charAt(binaryString.length()-1)));
            String binaryValue = binaryString.substring(1,binaryString.length()-1);
            int[] arrayIndex = sBoxRowCol(index,binaryValue);
            assert arrayIndex != null;
            return sBox[arrayIndex[0]][arrayIndex[1]];//its get value from sbox and return
        }catch (ArrayIndexOutOfBoundsException e)
        {
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    //find sbox row and column
    private int[] sBoxRowCol(String r,String c)
    {
        int row=0,col=0;
        try {
            if(r.charAt(0)=='0'&&r.charAt(1)=='0')
                row=0;
            else if(r.charAt(0)=='0'&&r.charAt(1)=='1')
                row=1;
            else if(r.charAt(0)=='1'&&r.charAt(1)=='0')
                row=2;
            else if(r.charAt(0)=='1'&&r.charAt(1)=='1')
                row=3;


            if(c.charAt(0)=='0'&&c.charAt(1)=='0'&&c.charAt(2)=='0'&&c.charAt(3)=='0')
                col=0;
            else  if(c.charAt(0)=='0'&&c.charAt(1)=='0'&&c.charAt(2)=='0'&&c.charAt(3)=='1')
                col=1;
            else  if(c.charAt(0)=='0'&&c.charAt(1)=='0'&&c.charAt(2)=='1'&&c.charAt(3)=='0')
                col=2;
            else  if(c.charAt(0)=='0'&&c.charAt(1)=='0'&&c.charAt(2)=='1'&&c.charAt(3)=='1')
                col=3;
            else  if(c.charAt(0)=='0'&&c.charAt(1)=='1'&&c.charAt(2)=='0'&&c.charAt(3)=='0')
                col=4;
            else  if(c.charAt(0)=='0'&&c.charAt(1)=='1'&&c.charAt(2)=='0'&&c.charAt(3)=='1')
                col=5;
            else  if(c.charAt(0)=='0'&&c.charAt(1)=='1'&&c.charAt(2)=='1'&&c.charAt(3)=='0')
                col=6;
            else  if(c.charAt(0)=='0'&&c.charAt(1)=='1'&&c.charAt(2)=='1'&&c.charAt(3)=='1')
                col=7;
            else  if(c.charAt(0)=='1'&&c.charAt(1)=='0'&&c.charAt(2)=='0'&&c.charAt(3)=='0')
                col=8;
            else  if(c.charAt(0)=='1'&&c.charAt(1)=='0'&&c.charAt(2)=='0'&&c.charAt(3)=='1')
                col=9;
            else  if(c.charAt(0)=='1'&&c.charAt(1)=='0'&&c.charAt(2)=='1'&&c.charAt(3)=='0')
                col=10;
            else  if(c.charAt(0)=='1'&&c.charAt(1)=='0'&&c.charAt(2)=='1'&&c.charAt(3)=='1')
                col=11;
            else  if(c.charAt(0)=='1'&&c.charAt(1)=='1'&&c.charAt(2)=='0'&&c.charAt(3)=='0')
                col=12;
            else  if(c.charAt(0)=='1'&&c.charAt(1)=='1'&&c.charAt(2)=='0'&&c.charAt(3)=='1')
                col=13;
            else  if(c.charAt(0)=='1'&&c.charAt(1)=='1'&&c.charAt(2)=='1'&&c.charAt(3)=='0')
                col=14;
            else  if(c.charAt(0)=='1'&&c.charAt(1)=='1'&&c.charAt(2)=='1'&&c.charAt(3)=='1')
                col=15;
            return new int[]{row,col};
        }catch (ArrayIndexOutOfBoundsException e)
        {
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    //convert 64 to 32 bit
    private String convertTo32Bit(String value)
    {
        StringBuilder convertValue = new StringBuilder();
        for(int i=0;i<value.length();i++)
        {
            if(i%2==0)
                convertValue.append(value.charAt(i));
        }

        return initialPermutation(convertValue.toString());
    }

    //binary to hexa
    private String convertBinaryToHex(String c)
    {
        StringBuilder hex = new StringBuilder();

        if(c.charAt(0)=='0'&&c.charAt(1)=='0'&&c.charAt(2)=='0'&&c.charAt(3)=='0')
            hex.append("0");
        else  if(c.charAt(0)=='0'&&c.charAt(1)=='0'&&c.charAt(2)=='0'&&c.charAt(3)=='1')
            hex.append("1");
        else  if(c.charAt(0)=='0'&&c.charAt(1)=='0'&&c.charAt(2)=='1'&&c.charAt(3)=='0')
            hex.append("2");
        else  if(c.charAt(0)=='0'&&c.charAt(1)=='0'&&c.charAt(2)=='1'&&c.charAt(3)=='1')
            hex.append("3");
        else  if(c.charAt(0)=='0'&&c.charAt(1)=='1'&&c.charAt(2)=='0'&&c.charAt(3)=='0')
            hex.append("4");
        else  if(c.charAt(0)=='0'&&c.charAt(1)=='1'&&c.charAt(2)=='0'&&c.charAt(3)=='1')
            hex.append("5");
        else  if(c.charAt(0)=='0'&&c.charAt(1)=='1'&&c.charAt(2)=='1'&&c.charAt(3)=='0')
            hex.append("6");
        else  if(c.charAt(0)=='0'&&c.charAt(1)=='1'&&c.charAt(2)=='1'&&c.charAt(3)=='1')
            hex.append("7");
        else  if(c.charAt(0)=='1'&&c.charAt(1)=='0'&&c.charAt(2)=='0'&&c.charAt(3)=='0')
            hex.append("8");
        else  if(c.charAt(0)=='1'&&c.charAt(1)=='0'&&c.charAt(2)=='0'&&c.charAt(3)=='1')
            hex.append("9");
        else  if(c.charAt(0)=='1'&&c.charAt(1)=='0'&&c.charAt(2)=='1'&&c.charAt(3)=='0')
            hex.append("A");
        else  if(c.charAt(0)=='1'&&c.charAt(1)=='0'&&c.charAt(2)=='1'&&c.charAt(3)=='1')
            hex.append("B");
        else  if(c.charAt(0)=='1'&&c.charAt(1)=='1'&&c.charAt(2)=='0'&&c.charAt(3)=='0')
            hex.append("C");
        else  if(c.charAt(0)=='1'&&c.charAt(1)=='1'&&c.charAt(2)=='0'&&c.charAt(3)=='1')
            hex.append("D");
        else  if(c.charAt(0)=='1'&&c.charAt(1)=='1'&&c.charAt(2)=='1'&&c.charAt(3)=='0')
            hex.append("E");
        else  if(c.charAt(0)=='1'&&c.charAt(1)=='1'&&c.charAt(2)=='1'&&c.charAt(3)=='1')
            hex.append("F");

        return hex.toString();
    }
}
