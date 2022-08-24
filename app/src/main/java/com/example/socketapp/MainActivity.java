package com.example.socketapp;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    Thread Thread1 = null;
    EditText etIP, etPort;
    TextView tvMessages;
    EditText etMessage;
    EditText vtMessage;
    Button btnSend,btnConnect;
    String SERVER_IP;
    int SERVER_PORT;
    String logtag = "UR3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.htMessage);
        vtMessage = findViewById(R.id.vtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessages.setText("");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(logtag,"execution started");
                    btnSend.setEnabled(false);


                    String cmd_up  = "movej([1.633, -0.939, -0.1890, -1.4545, -0.1415462, 0.936], a=1, v=0.6)" + " \n";
                    if (!cmd_up.isEmpty()) {
                        new Thread(new Thread3(cmd_up,true)).start();
                    }

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            String cmd_down = "movej([-0.0397, -0.03, -0.2399, -1.5617, -1.5779, 0.93602], a=1, v=0.6)" + " \n";
                            if (!cmd_down.isEmpty()) {
                                new Thread(new Thread3(cmd_down, false)).start();
                            }
                            Log.d(logtag,"execution end");
                        }
                    }, 5000);


                    //btnSend.setEnabled(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private PrintWriter output;
    private BufferedReader input;
    class Thread1 implements Runnable {
        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Connected\n");

                        btnSend.setEnabled(true);
                        btnConnect.setEnabled(false);
                        Toast.makeText(getApplicationContext(),"Connected !", Toast.LENGTH_SHORT).show();
                    }
                });
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Error ! Can't connect server", Toast.LENGTH_SHORT).show();
                        btnConnect.setEnabled(true);

                    }
                });
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessages.append("server: " + message + "\n");
                            }
                        });
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread3 implements Runnable {
        private String message;
        private boolean isDisable;
        Thread3(String message, boolean b) {
            this.message = message;
            this.isDisable = b;
        }
        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("client: " + message + "\n");
                    etMessage.setText("");
                    Log.d(logtag,"isDisable : "+isDisable);
                    Log.d(logtag,"Message : "+message);
                    if(isDisable)
                    {
                        btnSend.setEnabled(false);
                        Toast.makeText(getApplicationContext(), "Executing UR3", Toast.LENGTH_LONG).show();
                    }
                    else
                        btnSend.setEnabled(true);
                }
            });
        }
    }
}

//s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) s.connect((HOST, PORT)) cmd= "movej([1.633, -0.939, -0.1890, -1.4545, -0.1415462, 0.936], a=1, v=0.6)" + "\n" s.send (cmd.encode('utf-8'))