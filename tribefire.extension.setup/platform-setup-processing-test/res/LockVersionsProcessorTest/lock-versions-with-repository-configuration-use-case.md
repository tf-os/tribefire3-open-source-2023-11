graph TB
	subgraph com.braintribe.gm
		GROUP1[com.braintribe.gm] --> PARENT1(parent)--> PARENT1_10_1(1.0.1) & PARENT1_11_1(1.1.1)

		GROUP1 --> A1(a1) & A2(a2) & A3(a3) & A4(a4)
			A1 --> A1_10(1.0) --> A1_10_1(1.0.1) & A1_10_2(1.0.2)
			A2 --> A2_10(1.0) --> A2_10_1(1.0.1) & A2_10_2(1.0.2) & A2_10_3(1.0.3)
			A3 --> A3_10(1.0) & A3_11(1.1)
				A3_10 --> A3_10_3(1.0.3)
				A3_11 --> A3_11_3(1.1.3)
			A4 --> A4_10(1.1) --> A4_11_4(1.1.4)
	end


	subgraph tribefire.cortex
		GROUP2[tribefire.cortex] --> PARENT2(parent)--> PARENT2_20_2(2.0.2) & PARENT2_21_100(2.1.100) & PARENT2_22_200(2.2.200)
		GROUP2 --> B1(b1) & B2(b2)
			B1 --> B1_20(2.0) & B1_21(2.1) & B1_22(2.2)
				B1_20 --> B1_20_10(2.0.10) & B1_20_11(2.0.11) & B1_20_13(2.0.13)
				B1_21 --> B1_21_10(2.1.10) & B1_21_11(2.1.11)
				B1_22 --> B1_22_20(2.2.20) & B1_22_21(2.2.21)
			B2 --> B2_20(2.0) & B2_21(2.1)
				B2_20 --> B2_20_1(2.0.1) & B2_20_2(2.0.2) & B2_20_5(2.0.5)
				B2_21 --> B2_21_1(2.1.1) & B2_21_2(2.1.2) & B2_21_4(2.1.4)
	end

	subgraph use case
		USE_CASE>"Lock views: com.braintribe.gm:a4#1.1<br/><br/>We examine how lock-versions works with a repository configuration. First we get all locks without pointing to a repository configuration. This is visualized with green nodes.<br/>Then we point to a repository configuration and get all locks. In this case we restrict tribefire.cortex:b1 to version [2.1,2.2). This is visualized with pink nodes."]
	end

A4_11_4 -. "refers to" .-> PARENT1_11_1
A4_11_4 -. "depends on b1 [2,3) without repository configuration" .-> B1_22_21
B1_22_21 -. "refers to" .-> PARENT2_22_200

style A4_11_4 fill:#FF1493
style PARENT1_11_1 fill:#00FF00
style B1_22_21 fill:#008000
style PARENT2_22_200 fill:#008000

A4_11_4 -. "depends on b1 [2,3) with repository configuration" .-> B1_21_11
B1_21_11 -. "refers to" .-> PARENT2_21_100
B1_21_11 -. "depends on b2 [2.1,2.2)" .-> B2_21_4

style B1_21_11 fill:#FFC0CB
style B2_21_4 fill:#FFC0CB
style PARENT2_21_100 fill:#FFC0CB
